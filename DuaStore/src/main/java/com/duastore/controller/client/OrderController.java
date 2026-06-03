package com.duastore.controller.client;

import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.Order;
import com.duastore.service.client.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/tai-khoan/don-hang")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String trangThai,
                             HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/dang-nhap";

        Page<Order> orderPage;
        if (trangThai != null && !trangThai.isBlank()) {
            orderPage = orderService.getOrdersByUserIdAndStatus(userId, trangThai, page, 10);
        } else {
            orderPage = orderService.getOrdersByUserId(userId, page, 10);
        }

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("title", "Lịch sử đơn hàng");
        return "view/client/order/order-history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/dang-nhap";

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
    public String cancelOrder(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/dang-nhap";

        try {
            orderService.cancelOrder(userId, id);
            ra.addFlashAttribute("successMsg", "Hủy đơn hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang/" + id;
    }
}
