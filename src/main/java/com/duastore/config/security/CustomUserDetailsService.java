package com.duastore.config.security;

import com.duastore.model.Permission;
import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
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

        boolean hasActiveRole = user.getRoles().stream().anyMatch(Role::getIsActive);
        boolean enabled = Boolean.TRUE.equals(user.getIsActive()) && hasActiveRole;
        boolean accountNonLocked = user.getLockedUntil() == null
                || !user.getLockedUntil().isAfter(java.time.LocalDateTime.now());

        // QUAN TRONG: khong throw DisabledException/LockedException truc tiep o day.
        // DaoAuthenticationProvider.retrieveUser() bao toan bo loi UserDetailsService
        // nem ra (tru UsernameNotFoundException) thanh InternalAuthenticationServiceException
        // — lam mat kieu that su cua exception, khien CustomAuthenticationFailureHandler
        // khong nhan dien duoc la "tai khoan bi khoa" ma bao nham thanh "sai mat khau"
        // (con tru nham luot thu con lai). Thay vao do, tra ve enabled/accountNonLocked
        // dung trang thai — Spring Security se tu throw DisabledException/LockedException
        // dung kieu ngay sau retrieveUser() thanh cong (khong bi bao loi nua).
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled, true, true, accountNonLocked,
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
