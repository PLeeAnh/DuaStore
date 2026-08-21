package com.duastore.service;

import com.duastore.model.LinkedAccount;
import com.duastore.model.User;
import com.duastore.repository.LinkedAccountRepository;
import com.duastore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
/**
 * Service chứa nghiệp vụ (business logic) xử lý tài khoản mạng xã hội liên kết.
 */
public class LinkedAccountService {

    private final LinkedAccountRepository linkedAccountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LinkedAccountService(LinkedAccountRepository linkedAccountRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.linkedAccountRepository = linkedAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<LinkedAccount> getLinkedAccounts(Integer userId) {
        return linkedAccountRepository.findByUserId(userId);
    }

    public LinkedAccount linkAccount(Integer userId, String usernameOrEmail, String password) {
        User target = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!passwordEncoder.matches(password, target.getPassword())) {
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }
        if (target.getId().equals(userId)) {
            throw new RuntimeException("Không thể tự liên kết với chính mình");
        }
        if (linkedAccountRepository.existsByUserIdAndLinkedUserId(userId, target.getId())) {
            throw new RuntimeException("Tài khoản đã được liên kết");
        }
        LinkedAccount la = new LinkedAccount();
        la.setUserId(userId);
        la.setLinkedUserId(target.getId());
        return linkedAccountRepository.save(la);
    }

    public void unlinkAccount(Integer userId, Integer linkId) {
        LinkedAccount la = linkedAccountRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên kết"));
        if (!la.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa liên kết này");
        }
        linkedAccountRepository.delete(la);
    }

    public void switchAccount(Integer currentUserId, Integer targetUserId, HttpServletRequest request) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        boolean isLinked = linkedAccountRepository.existsByUserIdAndLinkedUserId(currentUserId, targetUserId)
                || linkedAccountRepository.existsByUserIdAndLinkedUserId(targetUserId, currentUserId);
        if (!currentUserId.equals(targetUserId) && !isLinked) {
            throw new RuntimeException("Tài khoản không được liên kết");
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("loggedIn", true);
        newSession.setAttribute("userId", target.getId());
        newSession.setAttribute("userName", target.getHoTen());
        newSession.setAttribute("userInitial", target.getHoTen() != null && !target.getHoTen().isEmpty()
                ? String.valueOf(target.getHoTen().charAt(0)).toUpperCase() : "U");
        newSession.setAttribute("userEmail", target.getEmail());
        newSession.setAttribute("userUsername", target.getUsername());
        newSession.setAttribute("userPhone", target.getSoDienThoai());
        newSession.setAttribute("userAvatar", target.getAvatar());
        newSession.setAttribute("userNickname", target.getNickname());
        newSession.setAttribute("userStatus", target.getStatus());
        newSession.setAttribute("userEmailVisible", target.getEmailVisible());
        newSession.setAttribute("userPhoneVisible", target.getPhoneVisible());
        newSession.setAttribute("userEmailMarketing", target.getEmailMarketing());

        String role = target.getRoles().stream().findFirst().map(r -> r.getName()).orElse("USER");
        newSession.setAttribute("userRole", role);

        List<SimpleGrantedAuthority> authorities = target.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.getModule() + "_" + p.getAction()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication auth = new UsernamePasswordAuthenticationToken(target.getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
