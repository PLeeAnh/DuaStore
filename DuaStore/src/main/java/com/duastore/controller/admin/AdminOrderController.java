package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.OrderStatusDTO;
import com.duastore.model.Order;
import com.duastore.model.User;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminOrderService;
import com.duastore.service.client.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/don-hang")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;
    private final AdminLogService adminLogService;
    private final SecurityUtil securityUtil;

    public AdminOrderController(AdminOrderService adminOrderService,
                                OrderService orderService,
                                AdminLogService adminLogService,
                                SecurityUtil securityUtil) {
        this.adminOrderService = adminOrderService;
        this.orderService = orderService;
        this.adminLogService = adminLogService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "false") boolean tatCa,
                              Model model) {
        User admin = securityUtil.getCurrentUser();
        if (admin == null) return "redirect:/login";

        Page<Order> orderPage;
        if (tatCa) {
            orderPage = adminOrderService.getAllOrders(page, 20);
        } else {
            orderPage = adminOrderService.getMyOrders(admin.getId(), page, 20);
        }
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("orders", orderDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("title", "Quản lý đơn hàng");
        return "view/admin/order/order-list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        try {
            Order order = adminOrderService.getOrderById(id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            var logs = adminLogService.getLogsByOrder(id);
            var assignment = adminLogService.getAssignmentByOrder(id);

            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("logs", logs);
            model.addAttribute("assignment", assignment);
            model.addAttribute("statusDTO", new OrderStatusDTO());
            model.addAttribute("title", "Chi tiết đơn hàng");
            return "view/admin/order/order-detail";
        } catch (Exception e) {
            return "redirect:/admin/don-hang";
        }
    }

    @PostMapping("/{id}/cap-nhat-trang-thai")
    public String updateStatus(@PathVariable Integer id,
                                @Valid @ModelAttribute("statusDTO") OrderStatusDTO dto,
                                BindingResult result,
                                RedirectAttributes ra,
                                HttpServletRequest request) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Dữ liệu không hợp lệ");
            return "redirect:/admin/don-hang/" + id;
        }
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng");
                return "redirect:/admin/don-hang/" + id;
            }

            Order order = adminOrderService.getOrderById(id);
            String oldStatus = order.getTrangThaiDon();
            String oldPayment = order.getTrangThaiTT();

            adminOrderService.updateOrderStatusWithLog(id, dto.getTrangThaiDon(), oldStatus, admin, request);
            if (dto.getTrangThaiTT() != null && !dto.getTrangThaiTT().isBlank()) {
                adminOrderService.updatePaymentStatusWithLog(id, dto.getTrangThaiTT(), oldPayment, admin, request);
            }
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }
}
