package com.duastore.controller.admin;

import com.duastore.service.SiteSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/homepage-layout")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới trang chủ.
 */
public class AdminHomepageLayoutController {

    private final SiteSettingService siteSettingService;

    private static final String SETTINGS_KEY = "homepage_section_order";
    private static final String SETTINGS_GROUP = "homepage";

    public AdminHomepageLayoutController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).HOMEPAGE_READ)")
    public ResponseEntity<?> getLayout() {
        String json = siteSettingService.getValue(SETTINGS_KEY);
        if (json == null) {
            return ResponseEntity.ok(List.of(
                    Map.of("id", "slider", "label", "Slider", "enabled", true),
                    Map.of("id", "banner", "label", "Banner", "enabled", true),
                    Map.of("id", "featured-products", "label", "Sản phẩm nổi bật", "enabled", true),
                    Map.of("id", "categories", "label", "Danh mục nổi bật", "enabled", true),
                    Map.of("id", "flash-sale", "label", "Flash Sale", "enabled", true),
                    Map.of("id", "vouchers", "label", "Voucher nổi bật", "enabled", true),
                    Map.of("id", "collection", "label", "Bộ sưu tập", "enabled", true),
                    Map.of("id", "blog", "label", "Blog nổi bật", "enabled", true),
                    Map.of("id", "popup", "label", "Popup", "enabled", true)
            ));
        }
        return ResponseEntity.ok(json);
    }

    @PostMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).HOMEPAGE_UPDATE)")
    public ResponseEntity<?> saveLayout(@RequestBody String json) {
        siteSettingService.save(SETTINGS_KEY, json, SETTINGS_GROUP);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
