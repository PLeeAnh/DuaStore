package com.duastore.controller.admin;

import com.duastore.model.Order;
import com.duastore.service.admin.AdminDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public String home(Model model) {
        model.addAttribute("title", "dashboard");
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        model.addAttribute("todayOrders", dashboardService.getTodayOrders());
        model.addAttribute("monthlyRevenue", dashboardService.getMonthlyRevenue());
        model.addAttribute("totalCustomers", dashboardService.getTotalCustomers());

        Map<String, Long> statusCounts = dashboardService.getOrderStatusCounts();
        model.addAttribute("pendingOrders", statusCounts.get("CHO_XAC_NHAN"));
        model.addAttribute("confirmedOrders", statusCounts.get("DA_XAC_NHAN"));
        model.addAttribute("shippingOrders", statusCounts.get("DANG_GIAO"));
        model.addAttribute("deliveredOrders", statusCounts.get("DA_GIAO"));
        model.addAttribute("cancelledOrders", statusCounts.get("DA_HUY"));

        List<Order> recentOrders = dashboardService.getRecentOrders();
        model.addAttribute("recentOrders", recentOrders);

        return "view/admin/dashboard";
    }
}
