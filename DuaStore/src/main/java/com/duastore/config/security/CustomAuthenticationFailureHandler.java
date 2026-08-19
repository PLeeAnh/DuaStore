package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    public CustomAuthenticationFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        String message;
        boolean locked = false;

        User user = (username != null && !username.isBlank())
                ? userRepository.findByUsernameOrEmail(username.trim()).orElse(null)
                : null;

        if (exception instanceof org.springframework.security.authentication.LockedException) {
            locked = true;
            message = "Tài khoản tạm khóa do đăng nhập sai nhiều lần. Vui lòng thử lại sau 15 phút.";
        } else if (exception instanceof org.springframework.security.authentication.DisabledException) {
            message = "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.";
        } else {
            int remaining = MAX_FAILED_ATTEMPTS;
            if (user != null) {
                int attempts = (user.getFailedAttempts() == null ? 0 : user.getFailedAttempts()) + 1;
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                    user.setFailedAttempts(0);
                    userRepository.save(user);
                    locked = true;
                    message = "Đăng nhập sai quá nhiều lần. Tài khoản tạm khóa 15 phút.";
                } else {
                    user.setFailedAttempts(attempts);
                    userRepository.save(user);
                    remaining = MAX_FAILED_ATTEMPTS - attempts;
                    message = "Sai tên đăng nhập hoặc mật khẩu. Còn " + remaining + " lần thử.";
                }
            } else {
                message = "Sai tên đăng nhập hoặc mật khẩu";
            }
        }

        request.getSession().setAttribute("loginErrorMessage", message);
        request.getSession().setAttribute("loginLocked", locked);
        response.sendRedirect(request.getContextPath() + "/dang-nhap?error");
    }
}
