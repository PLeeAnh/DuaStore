package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.duastore.controller.client")
public class ClientNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(ClientNavbarAdvice.class);

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;
    private final NotificationRepository notificationRepository;

    public ClientNavbarAdvice(CartService cartService, WishlistService wishlistService,
                               SecurityUtil securityUtil,
                               NotificationRepository notificationRepository) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        model.addAttribute("myCart", java.util.List.of());
        model.addAttribute("myWishlist", java.util.List.of());
        model.addAttribute("likedIds", java.util.List.of());

        try {
            Integer readMaxId = (Integer) session.getAttribute("notifReadMaxId");
            List<Notification> allNotifs = notificationRepository.findTop5ByIsActiveTrueOrderByCreatedAtDesc();

            if (readMaxId != null && readMaxId > 0) {
                List<Notification> unread = allNotifs.stream()
                    .filter(n -> n.getId() > readMaxId)
                    .toList();
                model.addAttribute("recentNotifs", unread);
                model.addAttribute("notifCount", notificationRepository.countByIsActiveTrueAndIdGreaterThan(readMaxId));
            } else {
                model.addAttribute("recentNotifs", allNotifs);
                model.addAttribute("notifCount", notificationRepository.countByIsActiveTrue());
            }
        } catch (Exception e) {
            log.warn("Loi lay thong bao: {}", e.getMessage());
            model.addAttribute("recentNotifs", java.util.List.of());
            model.addAttribute("notifCount", 0L);
        }

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId == null) return;

            model.addAttribute("myCart", cartService.getItems(userId));
            model.addAttribute("myWishlist", wishlistService.getWishlistByUser(userId));
            model.addAttribute("likedIds", wishlistService.getLikedProductIds(userId));

        } catch (Exception e) {
            log.warn("Loi ClientNavbarAdvice: {}", e.getMessage());
        }
    }
    
}
