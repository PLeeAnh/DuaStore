package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/giao-dien")
public class AdminAppearanceController {

    private static final String GROUP = "appearance";

    private final SiteSettingService siteSettingService;

    public AdminAppearanceController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).APPEARANCE_READ)")
    public String edit(Model model) {
        model.addAttribute("settings", siteSettingService.getGroup(GROUP));
        model.addAttribute("title", "giao-dien");
        model.addAttribute("tab", "theme");
        return "view/admin/appearance/settings";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).APPEARANCE_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        try {
            siteSettingService.saveGroupFromParams(params, GROUP);
            ra.addFlashAttribute("successMsg", "Cập nhật giao diện thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/giao-dien";
    }
}
