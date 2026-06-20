package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.OrderStatusDTO;
import com.duastore.model.Order;
import com.duastore.model.User;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminOrderService;
import com.duastore.service.admin.OrderNoteService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.service.client.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final OrderStatusLogService orderStatusLogService;
    private final OrderNoteService orderNoteService;

    public AdminOrderController(AdminOrderService adminOrderService,
                                OrderService orderService,
                                AdminLogService adminLogService,
                                SecurityUtil securityUtil,
                                OrderStatusLogService orderStatusLogService,
                                OrderNoteService orderNoteService) {
        this.adminOrderService = adminOrderService;
        this.orderService = orderService;
        this.adminLogService = adminLogService;
        this.securityUtil = securityUtil;
        this.orderStatusLogService = orderStatusLogService;
        this.orderNoteService = orderNoteService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(defaultValue = "false") boolean tatCa,
                              @RequestParam(required = false) String q,
                              @RequestParam(required = false) String trangThai,
                              @RequestParam(name = "trangThaiTT", required = false) String trangThaiTT,
                              Model model) {
        User admin = securityUtil.getCurrentUser();
        if (admin == null) return "redirect:/login";

        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        String filterTT = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
        String filterTTTT = (trangThaiTT != null && !trangThaiTT.isBlank()) ? trangThaiTT : null;

        Page<Order> orderPage;
        if (tatCa) {
            orderPage = adminOrderService.getAllOrders(page, size, query, filterTT, filterTTTT);
        } else {
            orderPage = adminOrderService.getMyOrders(admin.getId(), page, size, query, filterTT, filterTTTT);
        }
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("orders", orderDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "đơn hàng");
        model.addAttribute("url", "/admin/don-hang");
        Map<String, Object> filterParams = new HashMap<>();
        filterParams.put("tatCa", tatCa);
        if (query != null) filterParams.put("q", query);
        if (filterTT != null) filterParams.put("trangThai", filterTT);
        if (filterTTTT != null) filterParams.put("trangThaiTT", filterTTTT);
        model.addAttribute("filterParams", filterParams);
        model.addAttribute("tatCa", tatCa);
        model.addAttribute("q", q);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("trangThaiTT", trangThaiTT);
        model.addAttribute("title", "Quản lý đơn hàng");
        return "view/admin/order/order-list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_READ)")
    public String orderDetail(@PathVariable Integer id, Model model) {
        try {
            User admin = securityUtil.getCurrentUser();
            Order order = adminOrderService.getOrderById(id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            var logs = adminLogService.getLogsByOrder(id);
            var assignment = adminLogService.getAssignmentByOrder(id);

            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("logs", logs);
            model.addAttribute("assignment", assignment);
            model.addAttribute("statusLogs", orderStatusLogService.getLogsByOrder(id));
            model.addAttribute("currentStep", com.duastore.util.OrderStatusUtil.getStepIndex(order.getTrangThaiDon()));
            model.addAttribute("notes", orderNoteService.getNotesByOrder(id));
            OrderStatusDTO statusDTO = new OrderStatusDTO();
            statusDTO.setTrangThaiDon(order.getTrangThaiDon());
            statusDTO.setTrangThaiTT(order.getTrangThaiTT());
            model.addAttribute("statusDTO", statusDTO);
            model.addAttribute("title", "Chi tiết đơn hàng");
            return "view/admin/order/order-detail";
        } catch (Exception e) {
            return "redirect:/admin/don-hang";
        }
    }

    @PostMapping("/{id}/cap-nhat-trang-thai")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
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

            if ("DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
                ra.addFlashAttribute("errorMsg", "Đơn đã hoàn thành, không thể thay đổi");
                return "redirect:/admin/don-hang/" + id;
            }

            String oldStatus = order.getTrangThaiDon();
            String oldPayment = order.getTrangThaiTT();

            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, dto.getTrangThaiDon(), oldStatus, admin, request);
            String msg;
            if ("DA_HUY".equals(dto.getTrangThaiDon())) {
                msg = "Đã xóa đơn hàng";
            } else {
                msg = "Cập nhật trạng thái thành công";
            }
            if (stockMsg != null) msg += ". " + stockMsg;
            ra.addFlashAttribute("successMsg", msg);
            if ("DA_HUY".equals(dto.getTrangThaiDon())) {
                return "redirect:/admin/don-hang";
            }
            if (dto.getTrangThaiTT() != null && !dto.getTrangThaiTT().isBlank() && !dto.getTrangThaiTT().equals(oldPayment)) {
                adminOrderService.updatePaymentStatusWithLog(id, dto.getTrangThaiTT(), oldPayment, admin, request);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }

    @PostMapping("/api/{id}/cap-nhat-trang-thai")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
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

            if ("DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
                result.put("success", false);
                result.put("message", "Đơn đã hoàn thành, không thể thay đổi");
                return ResponseEntity.ok(result);
            }

            String oldStatus = order.getTrangThaiDon();
            String stockMsg = adminOrderService.updateOrderStatusWithLog(id, trangThai, oldStatus, admin, request);
            String msg;
            if ("DA_HUY".equals(trangThai)) {
                msg = "Đã xóa đơn hàng";
            } else {
                msg = "Cập nhật trạng thái thành công";
            }
            if (stockMsg != null) msg += ". " + stockMsg;

            result.put("success", true);
            result.put("message", msg);
            result.put("oldStatus", oldStatus);
            result.put("newStatus", trangThai);
            if ("DA_HUY".equals(trangThai)) {
                result.put("deleted", true);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/ghi-chu")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ORDER_UPDATE)")
    public String addNote(@PathVariable Integer id,
                           @RequestParam("noiDung") String noiDung,
                           RedirectAttributes ra) {
        if (noiDung == null || noiDung.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Nội dung ghi chú không được để trống");
            return "redirect:/admin/don-hang/" + id;
        }
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng");
                return "redirect:/admin/don-hang/" + id;
            }
            Order order = adminOrderService.getOrderById(id);
            orderNoteService.addNote(order, admin, noiDung.trim());
            ra.addFlashAttribute("successMsg", "Đã thêm ghi chú");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }
}
