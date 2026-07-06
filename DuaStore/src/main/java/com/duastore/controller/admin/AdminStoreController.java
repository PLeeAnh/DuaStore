package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.MultiValueMap;

@Controller
@RequestMapping("/admin/cua-hang")
public class AdminStoreController {

    private static final String GROUP = "store";

    private final SiteSettingService siteSettingService;
    private final String googleMapsApiKey;

    public AdminStoreController(SiteSettingService siteSettingService,
            @Value("${google.maps.api.key}") String googleMapsApiKey) {
        this.siteSettingService = siteSettingService;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_READ)")
    public String edit(Model model) {
        model.addAttribute("settings", siteSettingService.getGroup(GROUP));
        model.addAttribute("title", "cua-hang");
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        return "view/admin/store/form";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        ra.addFlashAttribute("successMsg", "Cập nhật thông tin cửa hàng thành công");
        return "redirect:/admin/cua-hang";
    }
}
