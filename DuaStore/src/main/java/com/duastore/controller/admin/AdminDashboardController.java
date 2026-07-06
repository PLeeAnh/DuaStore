package com.duastore.controller.admin;

import com.duastore.model.Order;
import com.duastore.service.admin.AdminDashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).DASHBOARD_READ)")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size) {
        model.addAttribute("title", "dashboard");
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        model.addAttribute("todayOrders", dashboardService.getTodayOrders());
        model.addAttribute("totalOrders", dashboardService.getTotalOrders());
        model.addAttribute("monthlyRevenue", dashboardService.getMonthlyRevenue());
        model.addAttribute("totalCustomers", dashboardService.getTotalCustomers());
        model.addAttribute("activePromotions", dashboardService.getActivePromotions());

        Map<String, Long> statusCounts = dashboardService.getOrderStatusCounts();
        model.addAttribute("pendingOrders", statusCounts.get("CHO_XAC_NHAN"));
        model.addAttribute("totalPendingOrders", statusCounts.get("CHO_XAC_NHAN"));
        model.addAttribute("confirmedOrders", statusCounts.get("DA_XAC_NHAN"));
        model.addAttribute("shippingOrders", statusCounts.get("DANG_GIAO"));
        model.addAttribute("deliveredOrders", statusCounts.get("DA_GIAO"));
        model.addAttribute("completedOrders", statusCounts.get("DA_HOAN_THANH"));
        model.addAttribute("cancelledOrders", statusCounts.get("DA_HUY"));

        Page<Order> ordersPage = dashboardService.getRecentOrders(PageRequest.of(page, size));
        model.addAttribute("recentOrders", ordersPage.getContent());
        model.addAttribute("orderAssignments", dashboardService.getOrderAssignments(ordersPage.getContent()));
        model.addAttribute("currentPage", ordersPage.getNumber());
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalItems", ordersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "đơn hàng");

        model.addAttribute("dailyRevenue", dashboardService.getDailyRevenueLast7Days());
        model.addAttribute("topProducts", dashboardService.getTopSellingProducts(5));

        // Enhanced dashboard data
        model.addAttribute("statComparison", dashboardService.getStatComparison());
        model.addAttribute("previousWeekRevenue", dashboardService.getPreviousWeekRevenue());
        model.addAttribute("paymentMethodDistribution", dashboardService.getPaymentMethodDistribution());
        model.addAttribute("salesFunnel", dashboardService.getSalesFunnel());
        model.addAttribute("revenueGrowth", dashboardService.getRevenueGrowth());

        return "view/admin/dashboard";
    }
}
