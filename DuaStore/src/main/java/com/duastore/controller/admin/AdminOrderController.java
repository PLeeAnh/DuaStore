package com.duastore.controller.admin;

import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.OrderStatusDTO;
import com.duastore.model.Order;
import com.duastore.service.admin.AdminOrderService;
import com.duastore.service.client.OrderService;
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

    public AdminOrderController(AdminOrderService adminOrderService, OrderService orderService) {
        this.adminOrderService = adminOrderService;
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Order> orderPage = adminOrderService.getAllOrders(page, 20);
        List<OrderDTO> orderDTOs = orderPage.getContent().stream()
                .map(orderService::convertToDTO)
                .collect(Collectors.toList());

        model.addAttribute("orders", orderDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("title", "Quản lý đơn hàng");
        return "view/admin/order/order-list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        try {
            Order order = adminOrderService.getOrderById(id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
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
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Dữ liệu không hợp lệ");
            return "redirect:/admin/don-hang/" + id;
        }
        try {
            adminOrderService.updateOrderStatus(id, dto.getTrangThaiDon());
            if (dto.getTrangThaiTT() != null && !dto.getTrangThaiTT().isBlank()) {
                adminOrderService.updatePaymentStatus(id, dto.getTrangThaiTT());
            }
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/don-hang/" + id;
    }
}
