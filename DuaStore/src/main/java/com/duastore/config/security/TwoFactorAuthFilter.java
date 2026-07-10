package com.duastore.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

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
            if (!skip && session != null) {
                Boolean verified = (Boolean) session.getAttribute("2faVerified");
                Integer userId = (Integer) session.getAttribute("2faUserId");
                if (userId != null && !Boolean.TRUE.equals(verified)) {
                    response.sendRedirect(request.getContextPath() + "/admin/2fa/challenge");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
