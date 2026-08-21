package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.client.WishlistService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wishlist")
/**
 * Controller xử lý các request HTTP liên quan tới danh sách yêu thích.
 */
public class WishlistController {

    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;

    public WishlistController(WishlistService wishlistService, SecurityUtil securityUtil) {
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String wishlist(Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        model.addAttribute("title", "san-pham-yeu-thich");
        model.addAttribute("wishlistItems", wishlistService.getWishlistByUser(userId));
        model.addAttribute("likedProductIds", wishlistService.getLikedProductIds(userId));
        return "view/client/wishlist";
    }
}
