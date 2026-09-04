package com.duastore.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
/**
 * Xử lý khi đăng nhập Google (OAuth2) thất bại. Phân biệt lỗi "tài khoản bị khóa"
 * (ném ra bởi CustomOAuth2UserService với error code "account_locked") với các lỗi
 * khác, để trang chủ hiển thị đúng thông báo thay vì luôn báo chung chung
 * "Sai tên đăng nhập hoặc mật khẩu" (vốn chỉ đúng cho đăng nhập bằng form).
 */
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String kind = "google";
        if (exception instanceof OAuth2AuthenticationException oe) {
            if ("account_locked".equals(oe.getError().getErrorCode())) {
                kind = "locked";
                request.getSession().setAttribute("loginErrorMessage", oe.getError().getDescription());
            }
        }
        response.sendRedirect(request.getContextPath() + "/?loginError=" + kind);
    }
}
