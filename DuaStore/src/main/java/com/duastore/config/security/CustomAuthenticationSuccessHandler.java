package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        User user = null;

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Map<String, Object> attrs = oauth2User.getAttributes();
            String email = (String) attrs.get("email");
            if (email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }
        }

        if (user == null) {
            String username = authentication.getName();
            user = userRepository.findByUsername(username).orElse(null);
        }

        if (user != null) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("userInitial", user.getHoTen() != null && !user.getHoTen().isEmpty()
                    ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase() : "U");
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userUsername", user.getUsername());
            session.setAttribute("userPhone", user.getSoDienThoai());
        }

        if (session.getAttribute("cartMergeDone") == null) {
            session.setAttribute("cartMergeDone", true);
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
