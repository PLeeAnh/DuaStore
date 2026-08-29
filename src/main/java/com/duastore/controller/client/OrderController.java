package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.Order;
import com.duastore.service.client.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tai-khoan/don-hang")
/**
 * Controller xử lý các request HTTP liên quan tới đơn hàng.
 */
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtil securityUtil;

    public OrderController(OrderService orderService, SecurityUtil securityUtil) {
        this.orderService = orderService;
        this.securityUtil = securityUtil;
    }

    private Integer getUserId() {
        return securityUtil.getCurrentUserId();
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trangThai,
            Model model) {
        Integer userId = getUserId();

        Page<Order> orderPage;
        if (trangThai != null && !trangThai.isBlank()) {
            orderPage = orderService.getOrdersByUserIdAndStatus(userId, trangThai, page, size);
        } else {
            orderPage = orderService.getOrdersByUserId(userId, page, size);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("title", "Lịch sử đơn hàng");
        return "view/client/order/order-history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();

        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("title", "Chi tiết đơn hàng");
            return "view/client/order/order-detail";
        } catch (Exception e) {
            return "redirect:/tai-khoan/don-hang";
        }
    }

    @PostMapping("/huy/{id}")
    public String cancelOrder(@PathVariable Integer id,
            @RequestParam("lyDo") String lyDo,
            RedirectAttributes ra) {
        Integer userId = getUserId();

        try {
            orderService.cancelOrder(userId, id, lyDo);
            ra.addFlashAttribute("successMsg", "Hủy đơn hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang";
    }

    @PostMapping("/danh-dau-da-nhan/{id}")
    public String markReceived(@PathVariable Integer id, RedirectAttributes ra) {
        Integer userId = getUserId();
        try {
            orderService.markOrderReceived(userId, id);
            ra.addFlashAttribute("successMsg", "Cảm ơn bạn đã xác nhận đơn hàng!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang/" + id;
    }
}
