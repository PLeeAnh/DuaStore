package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.MultiValueMap;

@Controller
@RequestMapping("/admin/van-chuyen")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới vận chuyển.
 */
public class AdminShippingController {

    private static final String GROUP = "shipping";

    private final SiteSettingService siteSettingService;

    public AdminShippingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).SHIPPING_SETTING_READ)")
    public String edit(Model model) {
        model.addAttribute("settings", siteSettingService.getGroup(GROUP));
        model.addAttribute("title", "van-chuyen");
        return "view/admin/shipping/form";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).SHIPPING_SETTING_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        ra.addFlashAttribute("successMsg", "Cập nhật cấu hình vận chuyển thành công");
        return "redirect:/admin/van-chuyen";
    }
}
