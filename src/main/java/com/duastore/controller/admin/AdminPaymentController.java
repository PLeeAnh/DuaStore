package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.MultiValueMap;

@Controller
@RequestMapping("/admin/thanh-toan")
public class AdminPaymentController {

    private static final String GROUP = "payment";

    private final SiteSettingService siteSettingService;

    public AdminPaymentController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PAYMENT_SETTING_READ)")
    public String edit(Model model) {
        model.addAttribute("settings", siteSettingService.getGroup(GROUP));
        model.addAttribute("title", "thanh-toan");
        return "view/admin/payment/form";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PAYMENT_SETTING_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        ra.addFlashAttribute("successMsg", "Cập nhật cấu hình thanh toán thành công");
        return "redirect:/admin/thanh-toan";
    }
}
