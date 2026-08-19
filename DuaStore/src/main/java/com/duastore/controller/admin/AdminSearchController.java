package com.duastore.controller.admin;

import com.duastore.config.security.PermissionEnum;
import com.duastore.config.security.SecurityService;
import com.duastore.service.admin.AdminSuggestionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/search")
public class AdminSearchController {

    private final AdminSuggestionService suggestionService;
    private final SecurityService securityService;

    public AdminSearchController(AdminSuggestionService suggestionService,
            SecurityService securityService) {
        this.suggestionService = suggestionService;
        this.securityService = securityService;
    }

    /**
     * Autocomplete tìm kiếm cho các form admin.
     * type = product | variant | customer | order | attribute
     * VD: /admin/api/search?type=product&q=chai&limit=7
     */
    @GetMapping
    @PreAuthorize("isAuthenticated() and ("
            + "@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)"
            + " or @sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CUSTOMER_READ)"
            + " or @sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ))")
    public List<Map<String, Object>> search(
            @RequestParam String type,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "7") int limit) {
        if ("customer".equals(type) && !securityService.hasPermission(PermissionEnum.CUSTOMER_READ)) {
            throw new AccessDeniedException("Bạn không có quyền xem khách hàng");
        }
        if ("order".equals(type) && !securityService.hasPermission(PermissionEnum.ORDER_READ)) {
            throw new AccessDeniedException("Bạn không có quyền xem đơn hàng");
        }
        if (!"customer".equals(type) && !"order".equals(type)
                && !securityService.hasPermission(PermissionEnum.PRODUCT_READ)) {
            throw new AccessDeniedException("Bạn không có quyền xem sản phẩm");
        }
        return suggestionService.search(type, q, limit);
    }
}