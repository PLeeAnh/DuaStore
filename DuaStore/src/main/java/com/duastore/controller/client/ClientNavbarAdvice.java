package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import com.duastore.service.SiteSettingService;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;
import java.util.Set;

@ControllerAdvice(basePackages = "com.duastore.controller.client")
public class ClientNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(ClientNavbarAdvice.class);

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;
    private final NotificationRepository notificationRepository;
    private final SiteSettingService siteSettingService;

    public ClientNavbarAdvice(CartService cartService, WishlistService wishlistService,
                               SecurityUtil securityUtil,
                               NotificationRepository notificationRepository,
                               SiteSettingService siteSettingService) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
        this.notificationRepository = notificationRepository;
        this.siteSettingService = siteSettingService;
    }

    @SuppressWarnings("unchecked")
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        model.addAttribute("myCart", java.util.List.of());
        model.addAttribute("myWishlist", java.util.List.of());
        model.addAttribute("likedIds", java.util.List.of());

        try {
            Integer readMaxId = (Integer) session.getAttribute("notifReadMaxId");
            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("notifReadIds");
            final Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : java.util.Collections.emptySet();
            List<Notification> allNotifs = notificationRepository.findCustomerNotifications();

            if (readMaxId != null && readMaxId > 0) {
                List<Notification> unread = allNotifs.stream()
                    .filter(n -> n.getId() > readMaxId && !readIds.contains(n.getId()))
                    .toList();
                model.addAttribute("recentNotifs", unread);
                long count = allNotifs.stream()
                    .filter(n -> n.getId() > readMaxId && !readIds.contains(n.getId()))
                    .count();
                model.addAttribute("notifCount", count);
            } else {
                List<Notification> unread = allNotifs.stream()
                    .filter(n -> !readIds.contains(n.getId()))
                    .toList();
                model.addAttribute("recentNotifs", unread);
                model.addAttribute("notifCount", (long) unread.size());
            }
        } catch (Exception e) {
            log.warn("Loi lay thong bao: {}", e.getMessage());
            model.addAttribute("recentNotifs", java.util.List.of());
            model.addAttribute("notifCount", 0L);
        }

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                model.addAttribute("myCart", cartService.getItems(userId));
                model.addAttribute("myWishlist", wishlistService.getWishlistByUser(userId));
                model.addAttribute("likedIds", wishlistService.getLikedProductIds(userId));
            }
        } catch (Exception e) {
            log.warn("Loi ClientNavbarAdvice: {}", e.getMessage());
        }

        // Inject appearance settings
        model.addAttribute("themeSettings", new java.util.HashMap<>());
        model.addAttribute("customCss", "");
        model.addAttribute("storeSettings", new java.util.HashMap<>());
        try {
            Map<String, String> themeSettings = siteSettingService.getGroup("appearance");
            model.addAttribute("themeSettings", themeSettings);
            model.addAttribute("customCss", siteSettingService.getValue("custom_css", ""));
            model.addAttribute("storeSettings", siteSettingService.getGroup("store"));
        } catch (Exception e) {
            log.warn("Loi load appearance settings: {}", e.getMessage());
        }
    }

}
