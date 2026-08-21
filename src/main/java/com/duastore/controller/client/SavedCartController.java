package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.client.SavedCartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gio-hang/da-luu")
/**
 * Controller xử lý các request HTTP liên quan tới giỏ hàng đã lưu, giỏ hàng.
 */
public class SavedCartController {

    private final SavedCartService savedCartService;
    private final SecurityUtil securityUtil;

    public SavedCartController(SavedCartService savedCartService, SecurityUtil securityUtil) {
        this.savedCartService = savedCartService;
        this.securityUtil = securityUtil;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String savedCart(Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        model.addAttribute("title", "san-pham-da-luu");
        model.addAttribute("savedItems", savedCartService.getSavedItems(userId));
        model.addAttribute("savedCount", savedCartService.count(userId));
        return "view/client/saved-cart";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/save/{variantId}")
    public String saveForLater(@PathVariable Integer variantId) {
        savedCartService.saveForLater(securityUtil.getCurrentUserId(), variantId);
        return "redirect:/gio-hang";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/move/{id}")
    public String moveToCart(@PathVariable Integer id) {
        savedCartService.moveToCart(securityUtil.getCurrentUserId(), id);
        return "redirect:/gio-hang/da-luu";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove/{id}")
    public String remove(@PathVariable Integer id) {
        savedCartService.removeSaved(securityUtil.getCurrentUserId(), id);
        return "redirect:/gio-hang/da-luu";
    }
}
