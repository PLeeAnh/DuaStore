package com.duastore.controller.admin;

import com.duastore.service.admin.AdminAnalyticsService;
import com.duastore.service.admin.AdminDashboardService;
import com.duastore.service.admin.AdminVariantPredictionService;
import com.duastore.util.PeriodRangeUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin/phan-tich")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới thống kê/phân tích.
 */
public class AdminAnalyticsController {

    private final AdminAnalyticsService analyticsService;
    private final AdminVariantPredictionService predictionService;
    private final AdminDashboardService dashboardService;

    public AdminAnalyticsController(AdminAnalyticsService analyticsService,
                                     AdminVariantPredictionService predictionService,
                                     AdminDashboardService dashboardService) {
        this.analyticsService = analyticsService;
        this.predictionService = predictionService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ANALYTICS_READ)")
    public String page(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String period,
            Model model) {

        var range = PeriodRangeUtil.resolve(period, from, to);
        LocalDate fromDate = range.from();
        LocalDate toDate = range.to();

        model.addAttribute("title", "phan-tich");
        model.addAttribute("fromDate", fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("toDate", toDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute("activePeriod", (period != null && !period.isEmpty()) ? period : null);

        // Revenue tab
        model.addAttribute("totalRevenue", analyticsService.getTotalRevenue(fromDate, toDate));
        model.addAttribute("dailyRevenue", analyticsService.getDailyRevenue(fromDate, toDate));
        model.addAttribute("revenueByCategory", analyticsService.getRevenueByCategory(fromDate, toDate));
        model.addAttribute("avgOrderValue", analyticsService.getAvgOrderValue(fromDate, toDate));

        // Orders tab
        model.addAttribute("totalOrders", analyticsService.getTotalOrders(fromDate, toDate));
        model.addAttribute("orderStatusCounts", analyticsService.getOrderStatusCounts(fromDate, toDate));
        model.addAttribute("paymentMethodCounts", analyticsService.getPaymentMethodCounts(fromDate, toDate));
        model.addAttribute("completedOrders", analyticsService.getCompletedOrders(fromDate, toDate));
        model.addAttribute("cancelledOrders", analyticsService.getCancelledOrders(fromDate, toDate));
        model.addAttribute("codOrders", analyticsService.getPaymentCount("COD", fromDate, toDate));
        model.addAttribute("onlineOrders", analyticsService.getOnlineOrderCount(fromDate, toDate));
        model.addAttribute("completionRate", analyticsService.getCompletionRate(fromDate, toDate));

        // Customers tab
        model.addAttribute("newCustomers", analyticsService.getNewCustomers(fromDate, toDate));
        model.addAttribute("topCustomers", analyticsService.getTopCustomers(fromDate, toDate));
        model.addAttribute("totalCustomers", analyticsService.getTotalCustomers());
        model.addAttribute("avgRevenuePerCustomer", analyticsService.getAvgRevenuePerCustomer(fromDate, toDate));
        model.addAttribute("customerLifetime", analyticsService.getCustomerLifetimeStats());
        model.addAttribute("rfmSegments", analyticsService.getRFMSegments());

        // Products tab
        model.addAttribute("topSellingProducts", analyticsService.getTopSellingProducts(fromDate, toDate));
        model.addAttribute("lowStockProducts", analyticsService.getLowStockProducts());
        model.addAttribute("totalStock", analyticsService.getTotalStock());
        model.addAttribute("totalProducts", analyticsService.getTotalProducts());
        model.addAttribute("restockPredictions", predictionService.getRestockRecommendations(30, 20));

        // Promotions tab
        model.addAttribute("activePromotions", analyticsService.getActivePromotions());
        model.addAttribute("voucherStats", analyticsService.getVoucherStats());
        model.addAttribute("topVouchers", analyticsService.getTopVouchers());
        model.addAttribute("totalDiscountGiven", analyticsService.getTotalDiscountGiven(fromDate, toDate));
        model.addAttribute("promotionEffectiveness", analyticsService.getPromotionEffectiveness(fromDate, toDate));

        // Margin / Profit tab
        model.addAttribute("marginSummary", analyticsService.getMarginSummary(fromDate, toDate));
        model.addAttribute("marginByCategory", analyticsService.getMarginByCategory(fromDate, toDate));
        model.addAttribute("topMarginProducts", analyticsService.getTopMarginProducts(fromDate, toDate, 10));

        // Traffic tab
        model.addAttribute("recentOrders", analyticsService.getRecentOrders(10));
        model.addAttribute("revenueByChannel", analyticsService.getRevenueByChannel(fromDate, toDate));
        model.addAttribute("conversionFunnel", analyticsService.getConversionFunnel(fromDate, toDate));
        model.addAttribute("topPages", analyticsService.getTopPages(fromDate, toDate));

        // Enhanced analytics from DashboardService
        model.addAttribute("monthlyRevenue12", dashboardService.getMonthlyRevenueLast12Months());
        model.addAttribute("salesFunnel", dashboardService.getSalesFunnel());
        model.addAttribute("cancelRefundRate", dashboardService.getCancelRate());
        model.addAttribute("urgentOrderCount", dashboardService.getUrgentOrderCount());
        model.addAttribute("revenueGrowth", dashboardService.getRevenueGrowth());
        model.addAttribute("topSelling7Days", dashboardService.getTopSellingProductsLast7Days(5));

        return "view/admin/analytics";
    }
}
