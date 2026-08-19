package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String resolveEmail(Authentication auth) {
        if (auth.getPrincipal() instanceof OAuth2User oauth2) {
            String email = (String) oauth2.getAttributes().get("email");
            if (email != null) {
                return email;
            }
        }
        return auth.getName();
    }

    public Integer getCurrentUserId() {
        return getCurrentUserIdOptional().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public Optional<Integer> getCurrentUserIdOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String email = resolveEmail(auth);
        // Use native query to avoid triggering Hibernate auto-flush during auditing callbacks
        try {
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT id FROM users WHERE email = :val OR username = :val LIMIT 1")
                    .setParameter("val", email)
                    .setMaxResults(1)
                    .getResultList();
            if (!rows.isEmpty()) {
                return Optional.of(((Number) rows.get(0)[0]).intValue());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String email = resolveEmail(auth);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return user;
        }
        return userRepository.findByUsername(email).orElse(null);
    }

    public Optional<User> getCurrentUserOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String email = resolveEmail(auth);
        return userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email));
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
