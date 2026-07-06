package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.Order;
import com.duastore.model.RefundRequest;
import com.duastore.repository.RefundRequestRepository;
import com.duastore.service.FileUploadService;
import com.duastore.service.admin.RefundService;
import com.duastore.service.client.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/tai-khoan/don-hang")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtil securityUtil;
    private final RefundService refundService;
    private final FileUploadService fileUploadService;

    public OrderController(OrderService orderService, SecurityUtil securityUtil,
            RefundService refundService, FileUploadService fileUploadService) {
        this.orderService = orderService;
        this.securityUtil = securityUtil;
        this.refundService = refundService;
        this.fileUploadService = fileUploadService;
    }

    private Integer getUserId() {
        return securityUtil.getCurrentUserId();
    }

    @GetMapping
    public String listOrders(@RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String trangThai,
            Model model) {
        Integer userId = getUserId();

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
    public String orderDetail(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();

        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            List<OrderItemDTO> items = orderService.getOrderItemsByOrder(order);
            boolean hasExistingRefund = refundService.hasRefundRequestByOrderId(id);
            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("hasExistingRefund", hasExistingRefund);
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

    @GetMapping("/hoan-tien/{id}")
    public String refundForm(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            OrderDTO orderDTO = orderService.convertToDTO(order);
            model.addAttribute("order", orderDTO);
            model.addAttribute("title", "Yêu cầu hoàn tiền");
            return "view/client/order/refund-form";
        } catch (Exception e) {
            return "redirect:/tai-khoan/don-hang";
        }
    }

    @PostMapping("/hoan-tien/{id}")
    public String submitRefund(@PathVariable Integer id,
            @RequestParam String lydo,
            @RequestParam BigDecimal soTienHoan,
            @RequestParam String phuongThucHoan,
            @RequestParam(required = false) MultipartFile anhMinhChung,
            RedirectAttributes ra) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            RefundRequest request = new RefundRequest();
            request.setOrderId(id);
            request.setUserId(userId);
            request.setLydo(lydo);
            request.setSoTienHoan(soTienHoan);
            request.setPhuongThucHoan(phuongThucHoan);
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                request.setAnhMinhChung(fileUploadService.save(anhMinhChung, "refunds"));
            }
            refundService.create(request);
            ra.addFlashAttribute("successMsg", "Gửi yêu cầu hoàn tiền thành công! Vui lòng chờ xử lý.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang/" + id;
    }
}
