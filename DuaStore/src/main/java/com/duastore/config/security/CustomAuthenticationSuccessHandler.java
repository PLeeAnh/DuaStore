package com.duastore.config.security;

import com.duastore.model.User;
import com.duastore.repository.UserAuthProviderRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.client.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final UserAuthProviderRepository userAuthProviderRepository;

    public CustomAuthenticationSuccessHandler(UserRepository userRepository, CartService cartService,
            UserAuthProviderRepository userAuthProviderRepository) {
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.userAuthProviderRepository = userAuthProviderRepository;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        User user = null;

        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Map<String, Object> attrs = oauth2User.getAttributes();
            String email = (String) attrs.get("email");
            if (email != null) {
                user = userRepository.findByEmailWithRoles(email).orElse(null);
            }
        }

        if (user == null) {
            String username = authentication.getName();
            user = userRepository.findByUsernameOrEmail(username).orElse(null);
        }

        if (user != null) {
            session.setAttribute("loggedIn", true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("userInitial", user.getHoTen() != null && !user.getHoTen().isEmpty()
                    ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase() : "U");
            session.setAttribute("userEmail", user.getEmail());
            String roleName = user.getRoles().stream()
                    .findFirst().map(r -> r.getName()).orElse("USER");
            session.setAttribute("userRole", roleName);
            session.setAttribute("userUsername", user.getUsername());
            session.setAttribute("userPhone", user.getSoDienThoai());
            session.setAttribute("userAvatar", user.getAvatar());
            session.setAttribute("userNickname", user.getNickname());
            session.setAttribute("userStatus", user.getStatus());
            session.setAttribute("userEmailVisible", user.getEmailVisible());
            session.setAttribute("userPhoneVisible", user.getPhoneVisible());
            session.setAttribute("userEmailMarketing", user.getEmailMarketing());
            session.setAttribute("userCreatedAt", user.getNgayTao());
            session.setAttribute("hasGoogleLinked", userAuthProviderRepository.existsByUserIdAndProvider(user.getId(), "GOOGLE"));
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");
        if (guestCart != null && !guestCart.isEmpty() && user != null) {
            cartService.mergeGuestCart(user.getId(), guestCart);
            session.removeAttribute("guestCart");
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
