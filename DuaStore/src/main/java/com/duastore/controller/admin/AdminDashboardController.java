package com.duastore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ★ AdminDashboardController — Dashboard Admin
 *  GET /admin → title="dashboard" → view/admin/dashboard
 *  Backend: thêm model attributes (totalProducts, todayOrders, monthlyRevenue, totalCustomers)
 *           hiện dùng "--" placeholder qua th:text="${x} ?: '--'"
 */
@Controller
public class AdminDashboardController {

    @GetMapping("/admin")
    public String home(Model model) {
        model.addAttribute("title", "dashboard");
        return "view/admin/dashboard";
    }
}
