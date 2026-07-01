package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.dto.CheckoutRequestDTO;
import com.duastore.model.Address;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.model.Promotion;
import com.duastore.model.User;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.model.OrderEventType;
import com.duastore.service.EmailService;
import com.duastore.service.PaymentService;
import com.duastore.service.ShippingFeeService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.service.client.CartService;
import com.duastore.service.client.OrderService;
import com.duastore.service.NotificationHelper;
import com.duastore.util.PriceUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final SecurityUtil securityUtil;
    private final ShippingFeeService shippingFeeService;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final OrderStatusLogService orderStatusLogService;
    private final NotificationHelper notificationHelper;

    public CheckoutController(OrderService orderService, CartService cartService,
                              AddressRepository addressRepository,
                              PromotionRepository promotionRepository,
                              SecurityUtil securityUtil,
                              ShippingFeeService shippingFeeService,
                              EmailService emailService,
                              PaymentService paymentService,
                              OrderStatusLogService orderStatusLogService,
                              NotificationHelper notificationHelper) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.securityUtil = securityUtil;
        this.shippingFeeService = shippingFeeService;
        this.emailService = emailService;
        this.paymentService = paymentService;
        this.orderStatusLogService = orderStatusLogService;
        this.notificationHelper = notificationHelper;
    }

    private Integer getUserId() {
        return securityUtil.getCurrentUserId();
    }

    @GetMapping
    public String showCheckout(Model model) {
        Integer userId = getUserId();
        List<CartItemDTO> cartItems = cartService.getItems(userId);
        if (cartItems.isEmpty()) return "redirect:/gio-hang";

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        BigDecimal subtotal = cartService.total(cartItems);
        BigDecimal phiShip = addresses.isEmpty()
                ? new BigDecimal("10000")
                : shippingFeeService.calculateFee(addresses.get(0), "SHIP");

        // Auto-apply best promotion
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, subtotal);
        BigDecimal tienGiam = BigDecimal.ZERO;
        String autoPromoCode = null;
        if (bestPromo != null) {
            tienGiam = orderService.calculateDiscount(bestPromo, subtotal);
            autoPromoCode = bestPromo.getMaCode();
            // Calculate per-item discounted prices
            for (CartItemDTO item : cartItems) {
                if (item.getGiaBan() != null) {
                    if ("PHAN_TRAM".equals(bestPromo.getLoaiGiam())) {
                        BigDecimal discountPct = bestPromo.getGiaTriGiam();
                        BigDecimal discountedPrice = item.getGiaBan()
                            .multiply(BigDecimal.valueOf(100).subtract(discountPct))
                            .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        item.setGiaBanSauGiam(discountedPrice);
                    } else {
                        // For fixed-amount, apply proportional discount
                        if (tienGiam.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal ratio = item.getGiaBan().multiply(tienGiam).divide(subtotal, java.math.RoundingMode.HALF_UP);
                            item.setGiaBanSauGiam(item.getGiaBan().subtract(ratio));
                        }
                    }
                }
            }
        }

        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setMaCode(autoPromoCode);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("tienGiam", tienGiam);
        model.addAttribute("bestPromo", bestPromo);
        model.addAttribute("tongTam", subtotal.add(phiShip).subtract(tienGiam));
        model.addAttribute("storeLat", shippingFeeService.getStoreLat());
        model.addAttribute("storeLng", shippingFeeService.getStoreLng());
        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("availablePromos", activePromotions);
        return "view/client/checkout";
    }

    @GetMapping("/shipping-fee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShippingFee(
            @RequestParam Integer addressId,
            @RequestParam(defaultValue = "SHIP") String method) {
        Map<String, Object> res = new HashMap<>();
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
            BigDecimal fee = shippingFeeService.calculateFee(address, method);
            res.put("fee", fee);
            res.put("success", true);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
                                   BindingResult result, Model model) {
        Integer userId = getUserId();

        if (result.hasErrors()) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            BigDecimal phiShip = addresses.isEmpty()
                    ? new BigDecimal("10000")
                    : shippingFeeService.calculateFee(addresses.get(0), "SHIP");
            BigDecimal tienGiam = calcAutoDiscount(subtotal);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("phiVanChuyen", phiShip);
            model.addAttribute("tienGiam", tienGiam);
            model.addAttribute("tongTam", subtotal.add(phiShip).subtract(tienGiam));
            model.addAttribute("storeLat", shippingFeeService.getStoreLat());
            model.addAttribute("storeLng", shippingFeeService.getStoreLng());
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }

        try {
            Order order = orderService.processCheckout(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu()
            );

            User finalUser = order.getUser();
            String finalTt = "CHUYEN_KHOAN".equals(order.getPhuongThucTT()) ? "Chuyển khoản" : "COD";
            String finalGh = "NHAN_TAI_CONG".equals(order.getPhuongThucGiaoHang()) ? "Nhận tại cửa hàng" : "Giao hàng tiêu chuẩn";
            String finalMaDon = order.getMaDon();
            String finalNgayDat = order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String finalDiaChi = order.getSnapDiaChi();
            String finalTongTien = PriceUtils.format(order.getTongThanhToan());

            StringBuilder itemsHtml = new StringBuilder();
            for (OrderItem item : order.getOrderItems()) {
                itemsHtml.append("<div style=\"display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f0f0f0;\">")
                        .append("<div><div style=\"font-size:14px;color:#424242;\">").append(item.getTenSanPham()).append("</div>")
                        .append("<div style=\"font-size:12px;color:#9e9e9e;\">").append(item.getTenBienThe()).append(" x ").append(item.getSoLuong()).append("</div></div>")
                        .append("<div style=\"font-size:14px;font-weight:600;color:#424242;\">").append(PriceUtils.format(item.getThanhTien())).append("</div></div>");
            }
            String finalItemsHtml = itemsHtml.toString();

            Thread emailThread = new Thread(() -> {
                try {
                    emailService.sendOrderSuccessEmail(
                            finalUser.getEmail(), finalUser.getHoTen(), finalMaDon,
                            finalNgayDat, finalDiaChi, finalTt, finalGh,
                            finalTongTien, finalItemsHtml
                    );
                } catch (Exception ignored) {}
            });
            emailThread.setDaemon(true);
            emailThread.start();

            if ("CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/chuyen-khoan/" + order.getId();
            }
            return "redirect:/checkout/thanh-cong/" + order.getId();
        } catch (RuntimeException e) {
            List<CartItemDTO> cartItems = cartService.getItems(userId);
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
            BigDecimal subtotal = cartService.total(cartItems);
            BigDecimal phiShip = addresses.isEmpty()
                    ? new BigDecimal("10000")
                    : shippingFeeService.calculateFee(addresses.get(0), "SHIP");
            BigDecimal tienGiam = calcAutoDiscount(subtotal);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("addresses", addresses);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("phiVanChuyen", phiShip);
            model.addAttribute("tienGiam", tienGiam);
            model.addAttribute("tongTam", subtotal.add(phiShip).subtract(tienGiam));
            model.addAttribute("storeLat", shippingFeeService.getStoreLat());
            model.addAttribute("storeLng", shippingFeeService.getStoreLng());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("title", "Thanh toán");
            return "view/client/checkout";
        }
    }

    private List<Promotion> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promos = promotionRepository.findActiveNow(now);
        if (promos.isEmpty()) {
            promos = promotionRepository.findByIsActiveTrue().stream()
                .filter(p -> p.getTuNgay() == null || !p.getTuNgay().isAfter(now))
                .filter(p -> p.getDenNgay() == null || !p.getDenNgay().isBefore(now))
                .toList();
        }
        return promos;
    }

    private BigDecimal calcAutoDiscount(BigDecimal subtotal) {
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, subtotal);
        if (bestPromo != null) {
            return orderService.calculateDiscount(bestPromo, subtotal);
        }
        return BigDecimal.ZERO;
    }

    private Promotion findBestPromo(List<Promotion> promos, BigDecimal subtotal) {
        BigDecimal maxPct = new BigDecimal("100");
        return promos.stream()
            .filter(p -> p.getDonHangToiThieu() == null || subtotal.compareTo(p.getDonHangToiThieu()) >= 0)
            .filter(p -> !"PHAN_TRAM".equals(p.getLoaiGiam()) || p.getGiaTriGiam().compareTo(maxPct) <= 0)
            .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
            .max(Comparator.comparing(p -> orderService.calculateDiscount(p, subtotal)))
            .orElse(null);
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiCreateOrder(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
                                                               BindingResult result) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = getUserId();
        if (userId == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        if (result.hasErrors()) {
            res.put("success", false);
            res.put("message", "Dữ liệu không hợp lệ");
            return ResponseEntity.ok(res);
        }
            try {
                Order order = orderService.processCheckout(
                        userId, req.getAddressId(), req.getPhuongThucTT(),
                        req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu()
                );
                if ("CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                    orderService.updatePaymentStatus(order.getId(), "DA_THANH_TOAN");
                }

                try {
                    notificationHelper.notifyStaff(
                        "Khách hàng vừa đặt đơn hàng mới: " + order.getMaDon(),
                        "ORDER", order.getId(),
                        "/admin/don-hang/" + order.getId(),
                        order.getMaDon()
                    );
                } catch (Exception ignored) {}

                res.put("success", true);
                res.put("orderId", order.getId());
                res.put("maDon", order.getMaDon());
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/api/qr-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiQrInfo(@RequestParam long amount) {
        Map<String, Object> res = new HashMap<>();
        res.put("qrUrl", paymentService.generateVietQrUrl("DUASTORE", amount));
        res.put("accountNumber", paymentService.getAccountNumber());
        res.put("accountName", paymentService.getAccountName());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/ap-dung-ma")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> applyPromo(@RequestParam String maCode,
                                                           @RequestParam BigDecimal subtotal) {
        Map<String, Object> res = new HashMap<>();
        try {
            Promotion promo = promotionRepository.findByMaCodeIgnoreCaseAndIsActiveTrue(maCode.trim())
                    .orElse(null);
            if (promo == null) {
                res.put("success", false);
                res.put("message", "Mã giảm giá không tồn tại hoặc đã ngừng hoạt động");
                return ResponseEntity.ok(res);
            }
            orderService.validatePromotion(promo, subtotal);
            BigDecimal tienGiam = orderService.calculateDiscount(promo, subtotal);
            res.put("success", true);
            res.put("tienGiam", tienGiam);
            res.put("message", "Áp dụng mã thành công! Giảm " +
                    (tienGiam.compareTo(BigDecimal.ZERO) > 0
                            ? PriceUtils.format(tienGiam) : "0₫"));
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/chuyen-khoan/{id}")
    public String chuyenKhoan(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/thanh-cong/" + id;
            }
            if ("DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
                return "redirect:/checkout/thanh-cong/" + id;
            }
            String qrUrl = paymentService.generateVietQrUrl(order.getMaDon(),
                    order.getTongThanhToan().longValue());
            model.addAttribute("order", orderService.convertToDTO(order));
            model.addAttribute("qrUrl", qrUrl);
            model.addAttribute("bankCode", paymentService.getBankCode());
            model.addAttribute("accountNumber", paymentService.getAccountNumber());
            model.addAttribute("accountName", paymentService.getAccountName());
            model.addAttribute("title", "Thanh toán chuyển khoản");
            return "view/client/payment";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }

    @PostMapping("/chuyen-khoan/{id}/xac-nhan")
    public String xacNhanChuyenKhoan(@PathVariable Integer id) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/thanh-cong/" + id;
            }
            orderService.updatePaymentStatus(id, "DA_THANH_TOAN");
            orderStatusLogService.ghiLog(order, OrderEventType.PAYMENT_CONFIRMED, null, null, null, null);

            try {
                notificationHelper.notifyStaff(
                    "Khách hàng đã xác nhận thanh toán cho đơn hàng: " + order.getMaDon(),
                    "ORDER", order.getId(),
                    "/admin/don-hang/" + order.getId(),
                    order.getMaDon()
                );
            } catch (Exception ignored) {}

                User finalUser = order.getUser();
                String finalTt2 = "Chuyển khoản";
                String finalGh2 = "NHAN_TAI_CONG".equals(order.getPhuongThucGiaoHang()) ? "Nhận tại cửa hàng" : "Giao hàng tiêu chuẩn";
                String finalMaDon2 = order.getMaDon();
                String finalNgayDat2 = order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                String finalDiaChi2 = order.getSnapDiaChi();
                String finalTongTien2 = PriceUtils.format(order.getTongThanhToan());

                StringBuilder itemsHtml = new StringBuilder();
                for (OrderItem item : order.getOrderItems()) {
                    itemsHtml.append("<div style=\"display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f0f0f0;\">")
                            .append("<div><div style=\"font-size:14px;color:#424242;\">").append(item.getTenSanPham()).append("</div>")
                            .append("<div style=\"font-size:12px;color:#9e9e9e;\">").append(item.getTenBienThe()).append(" x ").append(item.getSoLuong()).append("</div></div>")
                            .append("<div style=\"font-size:14px;font-weight:600;color:#424242;\">").append(PriceUtils.format(item.getThanhTien())).append("</div></div>");
                }
                String finalItemsHtml2 = itemsHtml.toString();

                Thread emailThread = new Thread(() -> {
                    try {
                        emailService.sendOrderSuccessEmail(
                                finalUser.getEmail(), finalUser.getHoTen(), finalMaDon2,
                                finalNgayDat2, finalDiaChi2, finalTt2, finalGh2,
                                finalTongTien2, finalItemsHtml2
                        );
                    } catch (Exception ignored) {}
                });
                emailThread.setDaemon(true);
                emailThread.start();

            return "redirect:/checkout/thanh-cong/" + id;
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/thanh-cong/{id}")
    public String orderSuccess(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            model.addAttribute("order", orderService.convertToDTO(order));
            model.addAttribute("title", "Đặt hàng thành công");
            return "view/client/order-success";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }
}
