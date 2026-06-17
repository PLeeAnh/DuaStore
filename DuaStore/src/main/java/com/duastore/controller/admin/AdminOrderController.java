package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.OrderStatusDTO;
import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.User;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminOrderService;
import com.duastore.service.client.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String trangThai,
                              Model model) {
        User admin = securityUtil.getCurrentUser();
        if (admin == null) return "redirect:/login";

        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        String filterTT = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;

        Page<Order> orderPage;
        if (tatCa) {
            orderPage = adminOrderService.getAllOrders(page, 20, query, filterTT);
        } else {
            orderPage = adminOrderService.getMyOrders(admin.getId(), page, 20, query, filterTT);
        }
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("orders", orderDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("q", q);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("title", "Quản lý đơn hàng");
        return "view/admin/order/order-list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        try {
            User admin = securityUtil.getCurrentUser();
            Order order = adminOrderService.getOrderById(id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            var logs = adminLogService.getLogsByOrder(id);
            var assignment = adminLogService.getAssignmentByOrder(id);

            boolean isAssignedAdmin = assignment != null && admin != null
                    && assignment.getAdmin().getId().equals(admin.getId());

            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("logs", logs);
            model.addAttribute("assignment", assignment);
            model.addAttribute("isAssignedAdmin", isAssignedAdmin);
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

            var assignment = adminLogService.getAssignmentByOrder(id);
            boolean isAssigned = assignment != null && assignment.getAdmin().getId().equals(admin.getId());
            boolean isCompleted = "DA_HOAN_THANH".equals(order.getTrangThaiDon());

            if (isCompleted && !isAssigned) {
                ra.addFlashAttribute("errorMsg", "Chỉ admin phụ trách mới có thể sửa đơn đã hoàn thành");
                return "redirect:/admin/don-hang/" + id;
            }

            String oldStatus = order.getTrangThaiDon();
            String oldPayment = order.getTrangThaiTT();

            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, dto.getTrangThaiDon(), oldStatus, admin, request);
            String msg = "Cập nhật trạng thái thành công.";
            if (stockMsg != null) msg += " " + stockMsg;
            ra.addFlashAttribute("successMsg", msg);
            if (dto.getTrangThaiTT() != null && !dto.getTrangThaiTT().isBlank()) {
                adminOrderService.updatePaymentStatusWithLog(id, dto.getTrangThaiTT(), oldPayment, admin, request);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/api/{id}/cap-nhat-trang-thai")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatusInline(@PathVariable Integer id,
                                                                    @RequestParam String trangThai,
                                                                    HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                result.put("success", false);
                result.put("message", "Chưa đăng nhập");
                return ResponseEntity.status(401).body(result);
            }

            Order order = adminOrderService.getOrderById(id);

            var assignment = adminLogService.getAssignmentByOrder(id);
            boolean isAssigned = assignment != null && assignment.getAdmin().getId().equals(admin.getId());
            boolean isCompleted = "DA_HOAN_THANH".equals(order.getTrangThaiDon());

            if (isCompleted && !isAssigned) {
                result.put("success", false);
                result.put("message", "Chỉ admin phụ trách mới có thể sửa đơn đã hoàn thành");
                return ResponseEntity.ok(result);
            }

            String oldStatus = order.getTrangThaiDon();
            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, trangThai, oldStatus, admin, request);
            String msg = "Cập nhật trạng thái thành công.";
            if (stockMsg != null) msg += " " + stockMsg;

            result.put("success", true);
            result.put("message", msg);
            result.put("oldStatus", oldStatus);
            result.put("newStatus", trangThai);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
