package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.User;
import com.duastore.service.admin.AdminDashboardService;
import com.duastore.service.admin.AdminOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Set;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "com.duastore.controller.admin")
/**
 * phía quản trị (admin) — Lớp @ControllerAdvice bổ sung dữ liệu/logic dùng chung cho các Controller liên quan tới admin controller advice.
 */
public class AdminControllerAdvice {

    private final AdminOrderService adminOrderService;
    private final AdminDashboardService adminDashboardService;
    private final SecurityUtil securityUtil;

    public AdminControllerAdvice(AdminOrderService adminOrderService,
            AdminDashboardService adminDashboardService,
            SecurityUtil securityUtil) {
        this.adminOrderService = adminOrderService;
        this.adminDashboardService = adminDashboardService;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute("pendingOrders")
    public long pendingOrders() {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                return 0;
            }
            return adminOrderService.countMyPendingOrders(admin.getId());
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("lowStockCount")
    public long lowStockCount() {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                return 0;
            }
            return adminDashboardService.getLowStockCount();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("userPermissions")
    public Set<String> getUserPermissions(Authentication auth) {
        if (auth == null) {
            return Set.of();
        }

        Set<String> roleAuthorities = Set.of("ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_USER");
        Set<String> perms = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !roleAuthorities.contains(a))
                .collect(Collectors.toSet());

        if (auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()))) {
            return Set.of("DASHBOARD_READ", "PRODUCT_READ", "CATEGORY_READ",
                    "ORDER_READ", "PROMOTION_READ", "POST_READ", "POST_CATEGORY_READ",
                    "REVIEW_READ", "USER_READ", "ROLE_READ", "AUDIT_LOG_READ",
                    "NOTIFICATION_READ", "ANALYTICS_READ", "CUSTOMER_READ",
                    "HOMEPAGE_READ", "STORE_READ", "APPEARANCE_READ",
                    "EMAIL_SETTING_READ", "PAYMENT_SETTING_READ", "SHIPPING_SETTING_READ",
                    "BANNER_READ", "FLASH_SALE_READ",
                    "VARIANT_READ", "PRICE_HISTORY_READ");
        }

        return perms;
    }
}
