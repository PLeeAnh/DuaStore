package com.duastore.controller.admin;

import com.duastore.service.EmailService;
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
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới email.
 */
public class AdminEmailController {

    private static final String GROUP = "email";

    private final SiteSettingService siteSettingService;
    private final EmailService emailService;

    public AdminEmailController(SiteSettingService siteSettingService, EmailService emailService) {
        this.siteSettingService = siteSettingService;
        this.emailService = emailService;
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

    @PostMapping("/test")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).EMAIL_SETTING_UPDATE)")
    @ResponseBody
    public Map<String, Object> test(@RequestParam MultiValueMap<String, String> params) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        boolean ok = emailService.sendTest();
        Map<String, Object> res = new HashMap<>();
        res.put("success", ok);
        return res;
    }
}
