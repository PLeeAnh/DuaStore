package com.duastore.controller.admin;

import com.duastore.model.Order;
import com.duastore.service.admin.AdminDashboardService;
import com.duastore.service.admin.AdminVariantPredictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminDashboardController {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);

    private final AdminDashboardService dashboardService;
    private final AdminVariantPredictionService predictionService;

    public AdminDashboardController(AdminDashboardService dashboardService,
                                     AdminVariantPredictionService predictionService) {
        this.dashboardService = dashboardService;
        this.predictionService = predictionService;
    }

    @GetMapping("/admin")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).DASHBOARD_READ)")
    public String home(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
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

            try {
                Page<Order> ordersPage = dashboardService.getRecentOrders(PageRequest.of(page, size));
                model.addAttribute("recentOrders", ordersPage.getContent());
                model.addAttribute("orderAssignments", dashboardService.getOrderAssignments(ordersPage.getContent()));
                model.addAttribute("currentPage", ordersPage.getNumber());
                model.addAttribute("totalPages", ordersPage.getTotalPages());
                model.addAttribute("totalItems", ordersPage.getTotalElements());
            } catch (Exception e) {
                log.error("Failed to load recent orders for dashboard", e);
                model.addAttribute("recentOrders", List.of());
                model.addAttribute("orderAssignments", Map.of());
                model.addAttribute("currentPage", 0);
                model.addAttribute("totalPages", 0);
                model.addAttribute("totalItems", 0L);
            }
            model.addAttribute("pageSize", size);
            model.addAttribute("entityLabel", "đơn hàng");

            model.addAttribute("dailyRevenue", dashboardService.getDailyRevenueLast7Days());
            model.addAttribute("topProducts", dashboardService.getTopSellingProducts(5));
            model.addAttribute("lowStockCount", dashboardService.getLowStockCount());
            model.addAttribute("lowStockProducts", dashboardService.getLowStockProducts(8));
            model.addAttribute("urgentOrderCount", dashboardService.getUrgentOrderCount());

            try {
                List<Map<String, Object>> allPredictions = predictionService.getRestockRecommendations(30, 50);
                List<Map<String, Object>> smartAlerts = allPredictions.stream()
                        .filter(p -> {
                            Object daysObj = p.get("daysUntilEmpty");
                            Object stockObj = p.get("stock");
                            double days = daysObj instanceof Number ? ((Number) daysObj).doubleValue() : 999;
                            int stock = stockObj instanceof Number ? ((Number) stockObj).intValue() : 0;
                            return days < 30 || stock < 5;
                        })
                        .collect(Collectors.toList());
                model.addAttribute("smartAlertCount", smartAlerts.size());
                model.addAttribute("smartAlerts", smartAlerts);
            } catch (Exception e) {
                log.error("Failed to load predictions for dashboard", e);
                model.addAttribute("smartAlertCount", 0);
                model.addAttribute("smartAlerts", List.of());
            }

            try {
                model.addAttribute("statComparison", dashboardService.getStatComparison());
                model.addAttribute("previousWeekRevenue", dashboardService.getPreviousWeekRevenue());
                model.addAttribute("paymentMethodDistribution", dashboardService.getPaymentMethodDistribution());
                model.addAttribute("salesFunnel", dashboardService.getSalesFunnel());
                model.addAttribute("revenueGrowth", dashboardService.getRevenueGrowth());
                model.addAttribute("monthlyRevenueData", dashboardService.getMonthlyRevenueLast12Months());
                model.addAttribute("topProducts7Days", dashboardService.getTopSellingProductsLast7Days(5));
                model.addAttribute("cancelRefundRate", dashboardService.getCancelRefundRate());
            } catch (Exception e) {
                log.error("Failed to load enhanced dashboard data", e);
            }
        } catch (Exception e) {
            log.error("Dashboard failed to load", e);
            model.addAttribute("dashboardError", "Lỗi tải dashboard: " + e.getMessage());
        }

        return "view/admin/dashboard";
    }
}
