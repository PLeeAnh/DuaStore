package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Component
/**
 * Lớp tiện ích (utility) hỗ trợ xử lý bảo mật/phân quyền.
 */
public class SecurityUtil {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtil.class);

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
        // Native query with FlushModeType.COMMIT: this runs from inside JPA Auditing's
        // @PreUpdate/@PrePersist callback (see AuditorConfig), i.e. mid-flush of some other
        // entity. The default AUTO flush mode auto-flushes the session before ANY query
        // (JPQL or native) executes, which re-enters the in-progress flush and corrupts
        // collection state (Hibernate throws "Found shared references to a collection").
        // COMMIT skips that pre-query flush; this lookup never needs to see uncommitted changes.
        try {
            List<Object> rows = entityManager.createNativeQuery(
                    "SELECT id FROM users WHERE email = :val OR username = :val")
                    .setParameter("val", email)
                    .setMaxResults(1)
                    .setFlushMode(FlushModeType.COMMIT)
                    .getResultList();
            if (!rows.isEmpty()) {
                return Optional.of(((Number) rows.get(0)).intValue());
            }
        } catch (Exception e) {
            log.warn("getCurrentUserIdOptional: lookup failed for principal", e);
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
                || a.getAuthority().equals("ROLE_PRODUCT_OWNER")
                || a.getAuthority().equals("ROLE_STAFF"));
    }
}
