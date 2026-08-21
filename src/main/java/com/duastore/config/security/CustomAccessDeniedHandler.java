package com.duastore.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
/**
 * Lớp xử lý (handler) cho custom access denied handler.
 */
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException e) throws IOException {
        if (request.getRequestURI().startsWith("/api/")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
        } else if (request.getRequestURI().startsWith("/admin/")) {
            response.sendRedirect(request.getContextPath() + "/admin/error/403");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
