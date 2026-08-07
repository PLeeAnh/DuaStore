package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

@Component
public class TwoFactorAuthFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PATHS = Set.of(
            "/admin/2fa/challenge",
            "/dang-xuat",
            "/admin/css/",
            "/admin/js/",
            "/css/",
            "/js/",
            "/images/"
    );

    private static final long TWOFA_TIMEOUT_MINUTES = 60;

    private final UserRepository userRepository;

    public TwoFactorAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated()
                && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (isAdmin && path.startsWith("/admin/")) {
            boolean skip = SKIP_PATHS.stream().anyMatch(p -> {
                if (p.endsWith("/")) {
                    return path.startsWith(p);
                }
                return path.equals(p) || path.startsWith(p);
            });
            if (!skip) {
                Integer userId = null;
                Boolean verified = null;
                String verifiedAtStr = null;
                if (session != null) {
                    userId = (Integer) session.getAttribute("2faUserId");
                    verified = (Boolean) session.getAttribute("2faVerified");
                    verifiedAtStr = (String) session.getAttribute("2faVerifiedAt");
                }

                // Remember-me (or other non-handler) login: 2faUserId never set.
                // Look up the user and enforce 2FA when enabled.
                if (userId == null) {
                    String username = auth.getName();
                    User u = userRepository.findByUsernameOrEmail(username).orElse(null);
                    if (u != null && Boolean.TRUE.equals(u.getTwoFactorEnabled())) {
                        if (session == null) {
                            session = request.getSession(true);
                        }
                        userId = u.getId();
                        session.setAttribute("2faUserId", userId);
                        session.setAttribute("2faVerified", false);
                        verified = false;
                    }
                }

                if (userId != null) {
                    if (!Boolean.TRUE.equals(verified)) {
                        if (session == null) {
                            session = request.getSession(true);
                        }
                        response.sendRedirect(request.getContextPath() + "/admin/2fa/challenge");
                        return;
                    }
                    if (Boolean.TRUE.equals(verified) && verifiedAtStr != null) {
                        Instant verifiedAt = Instant.parse(verifiedAtStr);
                        if (Instant.now().isAfter(verifiedAt.plusSeconds(TWOFA_TIMEOUT_MINUTES * 60))) {
                            session.removeAttribute("2faVerified");
                            session.removeAttribute("2faVerifiedAt");
                            response.sendRedirect(request.getContextPath() + "/admin/2fa/challenge");
                            return;
                        }
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}
