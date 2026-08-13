package com.duastore.controller.admin;

import com.duastore.service.FileUploadService;
import com.duastore.service.SiteSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.util.MultiValueMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/cua-hang")
public class AdminStoreController {

    private static final String GROUP = "store";

    private final SiteSettingService siteSettingService;
    private final FileUploadService fileUploadService;

    public AdminStoreController(SiteSettingService siteSettingService,
            FileUploadService fileUploadService) {
        this.siteSettingService = siteSettingService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_READ)")
    public String edit(Model model) {
        Map<String, String> settings = new java.util.HashMap<>(SiteSettingService.STORE_DEFAULTS);
        settings.putAll(siteSettingService.getGroup(GROUP));
        model.addAttribute("settings", settings);
        model.addAttribute("title", "cua-hang");
        return "view/admin/store/form";
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_UPDATE)")
    public String save(@RequestParam MultiValueMap<String, String> params, RedirectAttributes ra) {
        siteSettingService.saveGroupFromParams(params, GROUP);
        ra.addFlashAttribute("successMsg", "Cập nhật thông tin cửa hàng thành công");
        return "redirect:/admin/cua-hang";
    }

    @PostMapping("/upload")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_UPDATE)")
    @ResponseBody
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.save(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
