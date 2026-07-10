package com.duastore.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitingFilter extends HttpFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/dang-nhap",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/quen-mat-khau",
            "/dat-lai-mat-khau"
    );

    private final ConcurrentHashMap<String, Window> store = new ConcurrentHashMap<>();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String path = req.getRequestURI();
        if (!PROTECTED_PATHS.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        String key = ip(req) + ":" + path;
        long now = System.currentTimeMillis();

        Window w = store.compute(key, (k, v) -> {
            if (v == null || now - v.start > WINDOW_MS) return new Window(now, 1);
            v.count.incrementAndGet();
            return v;
        });

        if (w.count.get() > MAX_ATTEMPTS) {
            res.setStatus(429);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Quá nhiều yêu cầu, vui lòng thử lại sau\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private String ip(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        String xri = req.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank()) return xri.trim();
        return req.getRemoteAddr();
    }

    private record Window(long start, AtomicInteger count) {
        Window(long start, int count) { this(start, new AtomicInteger(count)); }
    }
}
