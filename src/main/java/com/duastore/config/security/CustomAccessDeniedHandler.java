package com.duastore.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
/**
 * Lớp xử lý (handler) cho custom access denied handler.
 * Phân biệt lỗi CSRF (phiên hết hạn / thiếu token) với lỗi access-denied thật
 * (không đủ quyền) để trả về đúng thông báo cho client, tránh việc mọi lỗi
 * 403 đều hiện popup "vui lòng đăng nhập" kể cả khi người dùng đã đăng nhập.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException e) throws IOException {
        boolean isCsrf = e instanceof CsrfException;
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (isCsrf) {
                response.getWriter().write("{\"success\":false,\"reason\":\"CSRF\",\"message\":\"Phiên làm việc đã hết hạn, vui lòng tải lại trang.\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"reason\":\"FORBIDDEN\",\"message\":\"Bạn không có quyền thực hiện thao tác này.\"}");
            }
        } else if (request.getRequestURI().startsWith("/admin/")) {
            response.sendRedirect(request.getContextPath() + "/admin/error/403");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
