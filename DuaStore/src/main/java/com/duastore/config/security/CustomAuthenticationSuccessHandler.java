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
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
            if (user.getFailedAttempts() != null && user.getFailedAttempts() > 0
                    || user.getLockedUntil() != null) {
                user.setFailedAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }
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
            try {
                session.setAttribute("hasGoogleLinked", userAuthProviderRepository.existsByUserIdAndProvider(user.getId(), "GOOGLE"));
            } catch (Exception ex) {
                session.setAttribute("hasGoogleLinked", false);
            }

            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> Set.of("ADMIN", "SUPER_ADMIN").contains(r.getName()));
            Boolean twoFactorEnabled = user.getTwoFactorEnabled();
            if (isAdmin && Boolean.TRUE.equals(twoFactorEnabled)) {
                session.setAttribute("2faUserId", user.getId());
                session.setAttribute("2faVerified", false);
            }
        }

        Boolean twoFactorVerified = (Boolean) session.getAttribute("2faVerified");
        boolean needs2fa = session.getAttribute("2faUserId") != null
                && !Boolean.TRUE.equals(twoFactorVerified);
        if (needs2fa) {
            response.sendRedirect(request.getContextPath() + "/admin/2fa/challenge");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");
        if (guestCart != null && !guestCart.isEmpty() && user != null) {
            try {
                cartService.mergeGuestCart(user.getId(), guestCart);
                session.removeAttribute("guestCart");
            } catch (Exception ex) {
                session.removeAttribute("guestCart");
            }
        }

        if (authentication.getPrincipal() instanceof OAuth2User) {
            response.sendRedirect(request.getContextPath() + "/oauth2/success");
            return;
        }

        SavedRequest savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if (savedRequest != null) {
            String savedUri = savedRequest.getRedirectUrl();
            String ctx = request.getContextPath();
            if (savedUri != null && (savedUri.contains(ctx + "/api/") || savedUri.contains(ctx + "/address/api/"))) {
                session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
