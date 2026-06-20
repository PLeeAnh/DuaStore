package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.duastore.controller.client")
public class ClientNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(ClientNavbarAdvice.class);

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;

    public ClientNavbarAdvice(CartService cartService, WishlistService wishlistService, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("myCart", java.util.List.of());
        model.addAttribute("myWishlist", java.util.List.of());
        model.addAttribute("likedIds", java.util.List.of());

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
