package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/email-smtp")
public class AdminEmailController {

    private static final String GROUP = "email";

    private final SiteSettingService siteSettingService;

    public AdminEmailController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).EMAIL_SETTING_READ)")
    public String edit(Model model) {
        Map<String, String> settings = new HashMap<>(SiteSettingService.EMAIL_DEFAULTS);
        settings.putAll(siteSettingService.getGroup(GROUP));
        model.addAttribute("settings", settings);
        model.addAttribute("title", "email-smtp");
        return "view/admin/email/form";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).EMAIL_SETTING_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        ra.addFlashAttribute("successMsg", "Cập nhật cấu hình email thành công");
        return "redirect:/admin/email-smtp";
    }
}
