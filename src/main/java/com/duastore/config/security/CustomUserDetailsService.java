package com.duastore.config.security;

import com.duastore.model.Permission;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý người dùng.
 */
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy tài khoản: " + username));

        if (!user.getIsActive()) {
            throw new DisabledException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new LockedException("Tài khoản tạm khóa do đăng nhập sai nhiều lần. Vui lòng thử lại sau 15 phút.");
        }

        boolean hasActiveRole = user.getRoles().stream().anyMatch(Role::getIsActive);
        if (!hasActiveRole) {
            throw new DisabledException(
                    "Tài khoản chưa được gán vai trò. Vui lòng liên hệ quản trị viên.");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                getAuthorities(user)
        );
    }

    public UserDetails loadUserByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy tài khoản: " + userId));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : user.getRoles()) {
            if (!role.getIsActive()) {
                continue;
            }

            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            if ("PRODUCT_OWNER".equals(role.getName())) {
                continue;
            }

            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(
                        perm.getModule() + "_" + perm.getAction()
                ));
            }
        }

        return authorities;
    }
}
