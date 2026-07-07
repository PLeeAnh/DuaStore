package com.duastore.controller.admin;

import com.duastore.service.FileUploadService;
import com.duastore.service.SiteSettingService;
import org.springframework.beans.factory.annotation.Value;
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
    private final String googleMapsApiKey;

    public AdminStoreController(SiteSettingService siteSettingService,
            FileUploadService fileUploadService,
            @Value("${google.maps.api.key}") String googleMapsApiKey) {
        this.siteSettingService = siteSettingService;
        this.fileUploadService = fileUploadService;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).STORE_READ)")
    public String edit(Model model) {
        model.addAttribute("settings", siteSettingService.getGroup(GROUP));
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
