package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.User;
import com.duastore.service.admin.AdminOrderService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.duastore.controller.admin")
public class AdminControllerAdvice {

    private final AdminOrderService adminOrderService;
    private final SecurityUtil securityUtil;

    public AdminControllerAdvice(AdminOrderService adminOrderService,
                                 SecurityUtil securityUtil) {
        this.adminOrderService = adminOrderService;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute("pendingOrders")
    public long pendingOrders() {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) return 0;
            return adminOrderService.countMyPendingOrders(admin.getId());
        } catch (Exception e) {
            return 0;
        }
    }
}
