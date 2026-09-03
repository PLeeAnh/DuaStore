package com.duastore.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servlet filter xử lý giới hạn tần suất request cho mỗi request.
 */
public class RateLimitingFilter extends HttpFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    // Chỉ giới hạn tần suất cho các endpoint THỰC SỰ nhạy cảm (đăng nhập, OTP, đặt lại mật
    // khẩu, tạo đơn hàng, đổi voucher...) — KHÔNG áp cho các thao tác UI bình thường như
    // giỏ hàng/yêu thích. Trước đây /api/cart/update nằm trong danh sách này, nên khách bấm
    // nút +/- số lượng vài lần liên tiếp (rất bình thường) là dính giới hạn 5 request/phút,
    // hiện lỗi "yêu cầu thất bại" dù không hề có gì bất thường — thao tác giỏ hàng không có
    // rủi ro dò mật khẩu/mã, không cần giới hạn kiểu chống brute-force này.
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/dang-nhap",
            "/api/auth/send-code",
            "/api/auth/verify-code",
            "/quen-mat-khau",
            "/dat-lai-mat-khau",
            "/tai-khoan/tai-khoan-lien-ket",
            "/checkout/api/create",
            "/address/api/save",
            "/api/coupon/validate"
    );

    private static final Set<String> PROTECTED_PREFIXES = Set.of(
            "/tai-khoan/chuyen-doi/",
            "/hoan-tien/",
            "/don-hang/huy/",
            "/api/vi-voucher/luu/",
            "/api/vi-voucher/xoa/"
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
        boolean protectedPath = PROTECTED_PATHS.contains(path)
                || PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
        if (!protectedPath) {
            chain.doFilter(req, res);
            return;
        }

        String key = ip(req) + ":" + path;
        long now = System.currentTimeMillis();

        if (store.size() > 10_000) {
            store.entrySet().removeIf(e -> now - e.getValue().start > WINDOW_MS);
        }

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
        // Khong tin X-Forwarded-For/X-Real-IP: day la header client tu gui duoc,
        // ke tan cong co the doi gia tri moi lan request de "reset" bucket va
        // vo hieu hoa gioi han brute-force. Chi dung dia chi TCP thuc te.
        return req.getRemoteAddr();
    }

    private record Window(long start, AtomicInteger count) {
        Window(long start, int count) { this(start, new AtomicInteger(count)); }
    }
}
