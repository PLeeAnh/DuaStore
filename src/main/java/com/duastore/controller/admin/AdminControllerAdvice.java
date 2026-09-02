package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.User;
import com.duastore.repository.PermissionRepository;
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
    private final PermissionRepository permissionRepository;

    public AdminControllerAdvice(AdminOrderService adminOrderService,
            AdminDashboardService adminDashboardService,
            SecurityUtil securityUtil,
            PermissionRepository permissionRepository) {
        this.adminOrderService = adminOrderService;
        this.adminDashboardService = adminDashboardService;
        this.securityUtil = securityUtil;
        this.permissionRepository = permissionRepository;
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

        Set<String> roleAuthorities = Set.of("ROLE_PRODUCT_OWNER", "ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER");
        Set<String> perms = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !roleAuthorities.contains(a))
                .collect(Collectors.toSet());

        if (auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PRODUCT_OWNER".equals(a.getAuthority()))) {
            // PRODUCT_OWNER co toan quyen he thong (khong duoc gan permission rieng le
            // trong CustomUserDetailsService), nen lay truc tiep tu bang permissions
            // thay vi liet ke tay - tranh bi lech khi co module/permission moi.
            return permissionRepository.findAllByOrderByModuleAscActionAsc().stream()
                    .map(p -> p.getModule() + "_" + p.getAction())
                    .collect(Collectors.toSet());
        }

        return perms;
    }
}
