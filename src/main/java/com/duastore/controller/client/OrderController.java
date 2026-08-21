package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.Order;
import com.duastore.model.RefundRequest;
import com.duastore.model.RefundReason;
import com.duastore.model.RefundType;
import com.duastore.service.FileUploadService;
import com.duastore.service.NotificationHelper;
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
import java.util.Optional;

@Controller
@RequestMapping("/tai-khoan/don-hang")
/**
 * Controller xử lý các request HTTP liên quan tới đơn hàng.
 */
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtil securityUtil;
    private final RefundService refundService;
    private final FileUploadService fileUploadService;
    private final NotificationHelper notificationHelper;

    public OrderController(OrderService orderService, SecurityUtil securityUtil,
            RefundService refundService, FileUploadService fileUploadService,
            NotificationHelper notificationHelper) {
        this.orderService = orderService;
        this.securityUtil = securityUtil;
        this.refundService = refundService;
        this.fileUploadService = fileUploadService;
        this.notificationHelper = notificationHelper;
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
            Optional<RefundRequest> existingRefund = refundService.getActiveRefundByOrderId(id);
            boolean hasExistingRefund = existingRefund.isPresent();
            model.addAttribute("order", orderDTO);
            model.addAttribute("items", items);
            model.addAttribute("hasExistingRefund", hasExistingRefund);
            model.addAttribute("refundId", existingRefund.map(RefundRequest::getId).orElse(null));
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
            model.addAttribute("refundTypes", RefundType.values());
            model.addAttribute("refundReasons", RefundReason.values());
            model.addAttribute("title", "Yêu cầu hoàn tiền / Đổi hàng");
            return "view/client/order/refund-form";
        } catch (Exception e) {
            return "redirect:/tai-khoan/don-hang";
        }
    }

    @PostMapping("/hoan-tien/{id}")
    public String submitRefund(@PathVariable Integer id,
            @RequestParam String lydo,
            @RequestParam String loaiYeuCau,
            @RequestParam String lyDoChiTiet,
            @RequestParam BigDecimal soTienHoan,
            @RequestParam String phuongThucHoan,
            @RequestParam(required = false) MultipartFile anhMinhChung,
            @RequestParam(required = false) MultipartFile videoUnboxing,
            @RequestParam(required = false) String tenNganHang,
            @RequestParam(required = false) String soTaiKhoan,
            @RequestParam(required = false) String chuTaiKhoan,
            RedirectAttributes ra) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            if (!"DA_GIAO".equals(order.getTrangThaiDon()) && !"DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
                throw new RuntimeException("Chỉ có thể yêu cầu hoàn tiền cho đơn hàng đã giao");
            }
            if (refundService.hasRefundRequestByOrderId(id)) {
                throw new RuntimeException("Đơn hàng này đã có yêu cầu hoàn tiền trước đó");
            }
            RefundType type = RefundType.fromCode(loaiYeuCau);
            RefundReason reason = RefundReason.fromCode(lyDoChiTiet);

            if (type == RefundType.HOAN_TIEN && (soTienHoan == null || soTienHoan.compareTo(BigDecimal.ZERO) <= 0
                    || soTienHoan.compareTo(order.getTongThanhToan()) > 0)) {
                throw new RuntimeException("Số tiền hoàn không hợp lệ, tối đa " + order.getTongThanhToan() + "đ");
            }

            if (reason.requiresVideoProof() && (videoUnboxing == null || videoUnboxing.isEmpty())) {
                throw new RuntimeException("Yêu cầu video unboxing cho lý do: " + reason.getDisplayName());
            }

            RefundRequest request = new RefundRequest();
            request.setOrderId(id);
            request.setUserId(userId);
            request.setLydo(lydo);
            request.setLoaiYeuCau(type.name());
            request.setLyDoChiTiet(reason.name());
            request.setSoTienHoan(soTienHoan);
            request.setPhuongThucHoan(phuongThucHoan);
            request.setTenNganHang(tenNganHang);
            request.setSoTaiKhoan(soTaiKhoan);
            request.setChuTaiKhoan(chuTaiKhoan);
            if (anhMinhChung != null && !anhMinhChung.isEmpty()) {
                request.setAnhMinhChung(fileUploadService.save(anhMinhChung, "refunds"));
            }
            if (videoUnboxing != null && !videoUnboxing.isEmpty()) {
                request.setVideoUnboxing(fileUploadService.saveVideo(videoUnboxing, "refunds/videos"));
            }
            RefundRequest savedRefund = refundService.submitRefundRequest(request);
            notificationHelper.notifyStaff(
                    "Khach hang yeu cau " + type.getDisplayName().toLowerCase() + " cho don " + order.getId(),
                    "ORDER", order.getId(),
                    "/admin/hoan-tien/detail/" + savedRefund.getId(),
                    "Xem yeu cau"
            );
            ra.addFlashAttribute("successMsg", "Gửi yêu cầu " + type.getDisplayName().toLowerCase() + " thành công! Vui lòng chờ xử lý.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang/" + id;
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

    @GetMapping("/hoan-tien/theo-doi/{refundId}")
    public String refundTracking(@PathVariable Integer refundId, Model model) {
        Integer userId = getUserId();
        try {
            RefundRequest refund = refundService.getById(refundId);
            if (!refund.getUserId().equals(userId)) {
                return "redirect:/tai-khoan/don-hang";
            }
            model.addAttribute("refund", refund);
            model.addAttribute("title", "Theo dõi hoàn tiền / đổi hàng");
            return "view/client/order/refund-tracking";
        } catch (Exception e) {
            return "redirect:/tai-khoan/don-hang";
        }
    }

    @PostMapping("/hoan-tien/update-tracking/{refundId}")
    public String updateReturnTracking(@PathVariable Integer refundId,
            @RequestParam String maVanDonTra,
            RedirectAttributes ra) {
        Integer userId = getUserId();
        try {
            RefundRequest refund = refundService.getById(refundId);
            if (!refund.getUserId().equals(userId)) {
                throw new RuntimeException("Không có quyền truy cập");
            }
            refund.setMaVanDonTra(maVanDonTra);
            refundService.create(refund);
            ra.addFlashAttribute("successMsg", "Đã cập nhật mã vận đơn trả hàng");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/tai-khoan/don-hang/hoan-tien/theo-doi/" + refundId;
    }
}
