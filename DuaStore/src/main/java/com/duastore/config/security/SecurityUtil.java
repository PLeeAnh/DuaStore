package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String resolveEmail(Authentication auth) {
        if (auth.getPrincipal() instanceof OAuth2User oauth2) {
            String email = (String) oauth2.getAttributes().get("email");
            if (email != null) return email;
        }
        return auth.getName();
    }

    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = resolveEmail(auth);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) return user.getId();
        return userRepository.findByUsername(email)
                .map(User::getId)
                .orElse(null);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = resolveEmail(auth);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) return user;
        return userRepository.findByUsername(email).orElse(null);
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
