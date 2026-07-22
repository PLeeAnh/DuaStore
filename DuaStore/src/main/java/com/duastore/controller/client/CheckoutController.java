package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.dto.CheckoutRequestDTO;
import com.duastore.dto.OrderDTO;
import com.duastore.model.Address;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.model.Promotion;
import com.duastore.model.User;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.model.OrderEventType;
import com.duastore.service.EmailService;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.PaymentService;
import com.duastore.dto.CarrierQuote;
import com.duastore.service.MultiCarrierShippingService;
import com.duastore.service.SiteSettingService;
import com.duastore.service.VNPAYService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.service.admin.FraudDetectionService;
import com.duastore.service.client.CartService;
import com.duastore.service.client.OrderService;
import com.duastore.service.client.VoucherWalletService;
import com.duastore.service.NotificationHelper;
import com.duastore.util.PriceUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final PaymentService paymentService;
    private final OrderStatusLogService orderStatusLogService;
    private final NotificationHelper notificationHelper;
    private final VNPAYService vnpayService;
    private final FraudDetectionService fraudDetectionService;
    private final VoucherWalletService voucherWalletService;
    private final LoyaltyPointsService loyaltyPointsService;
    private final SiteSettingService siteSettingService;
    private final MultiCarrierShippingService multiCarrierShippingService;

    @Value("${store.latitude}")
    private double storeLat;

    @Value("${store.longitude}")
    private double storeLng;

    public CheckoutController(OrderService orderService, CartService cartService,
            AddressRepository addressRepository,
            PromotionRepository promotionRepository,
            SecurityUtil securityUtil,
            EmailService emailService,
            PaymentService paymentService,
            OrderStatusLogService orderStatusLogService,
            NotificationHelper notificationHelper,
            VNPAYService vnpayService,
            FraudDetectionService fraudDetectionService,
            VoucherWalletService voucherWalletService,
            LoyaltyPointsService loyaltyPointsService,
            SiteSettingService siteSettingService,
            MultiCarrierShippingService multiCarrierShippingService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.securityUtil = securityUtil;
        this.emailService = emailService;
        this.paymentService = paymentService;
        this.orderStatusLogService = orderStatusLogService;
        this.notificationHelper = notificationHelper;
        this.vnpayService = vnpayService;
        this.fraudDetectionService = fraudDetectionService;
        this.voucherWalletService = voucherWalletService;
        this.loyaltyPointsService = loyaltyPointsService;
        this.siteSettingService = siteSettingService;
        this.multiCarrierShippingService = multiCarrierShippingService;
    }

    private Integer getUserId() {
        return securityUtil.getCurrentUserId();
    }

    @GetMapping
    public String showCheckout(Model model,
            @RequestParam(value = "selected", required = false) List<Integer> selected) {
        Integer userId = getUserId();
        List<CartItemDTO> cartItems = cartService.getItems(userId);
        if (selected != null && !selected.isEmpty()) {
            Set<Integer> selectedSet = new HashSet<>(selected);
            cartItems = cartItems.stream()
                    .filter(item -> item.getVariantId() != null && selectedSet.contains(item.getVariantId()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedIds", selected);
        }
        if (cartItems.isEmpty()) {
            return "redirect:/gio-hang";
        }

        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        BigDecimal subtotal = cartService.total(cartItems);
        BigDecimal phiShip = addresses.isEmpty()
                ? new BigDecimal("10000")
                : multiCarrierShippingService.calculateFeeForCarrier("GHN", addresses.get(0), subtotal);

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
        model.addAttribute("checkoutRequest", checkoutRequest);
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("userVouchers", userId != null ? voucherWalletService.getAvailableVouchers(userId) : List.of());
        model.addAttribute("loyaltyBalance", userId != null ? loyaltyPointsService.getBalance(userId) : 0);
        model.addAttribute("loyaltyRedeemRate", loyaltyPointsService.getPointsRedeemRate());
        model.addAttribute("loyaltyEarnRate", loyaltyPointsService.getPointsEarnRate());

        // Estimated delivery date
        model.addAttribute("estimatedDeliveryDate", java.time.LocalDate.now().plusDays(5).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " – " + java.time.LocalDate.now().plusDays(10).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Load payment method toggles from DB
        Map<String, String> paymentSettings = siteSettingService.getGroup("payment");
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.get("payment_cod")));
        paymentMethods.put("bank", "1".equals(paymentSettings.get("payment_bank")));
        paymentMethods.put("vnpay", "1".equals(paymentSettings.get("payment_vnpay")));
        model.addAttribute("paymentMethods", paymentMethods);

        // Load carrier settings
        Map<String, String> shippingSettings = siteSettingService.getGroup("shipping");
        model.addAttribute("carrierGHNEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghn_enabled", "1")));
        model.addAttribute("carrierGHTKEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghtk_enabled", "1")));

        model.addAttribute("storeLat", storeLat);
        model.addAttribute("storeLng", storeLng);

        // Set default payment method based on available methods
        if (!paymentMethods.get("cod") && paymentMethods.get("vnpay")) {
            model.addAttribute("defaultPaymentMethod", "VNPAY");
        } else if (!paymentMethods.get("cod") && paymentMethods.get("bank")) {
            model.addAttribute("defaultPaymentMethod", "CHUYEN_KHOAN");
        } else {
            model.addAttribute("defaultPaymentMethod", "COD");
        }
        return "view/client/checkout";
    }

    private void addPaymentMethodsToModel(Model model, Map<String, String> paymentSettings) {
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.get("payment_cod")));
        paymentMethods.put("bank", "1".equals(paymentSettings.get("payment_bank")));
        paymentMethods.put("vnpay", "1".equals(paymentSettings.get("payment_vnpay")));
        model.addAttribute("paymentMethods", paymentMethods);
    }

    @GetMapping("/shipping-fee")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShippingFee(
            @RequestParam Integer addressId) {
        Map<String, Object> res = new HashMap<>();
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
            BigDecimal fee = multiCarrierShippingService.calculateFeeForCarrier("GHN", address, null);
            res.put("fee", fee);
            res.put("success", true);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/api/quotes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getQuotes(
            @RequestParam Integer addressId,
            @RequestParam(required = false) BigDecimal subtotal) {
        Map<String, Object> res = new HashMap<>();
        try {
            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
            List<CarrierQuote> quotes = multiCarrierShippingService.getQuotes(address, subtotal);
            res.put("quotes", quotes);
            res.put("success", true);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public String processCheckout(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
            BindingResult result, Model model,
            HttpServletRequest httpReq) {
        Integer userId = getUserId();

        if (result.hasErrors()) {
            buildCheckoutModel(model, userId, req.getSelectedIds());
            return "view/client/checkout";
        }

        try {
            Set<Integer> selectedSet = req.getSelectedIds() != null && !req.getSelectedIds().isEmpty()
                    ? new HashSet<>(req.getSelectedIds()) : null;
            Order order = orderService.processCheckout(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu(),
                    req.getPointsToRedeem() != null ? req.getPointsToRedeem() : 0,
                    selectedSet,
                    req.getShippingCarrier() != null ? req.getShippingCarrier() : "GHN"
            );

            fraudDetectionService.analyzeAndPersist(order);

            try {
                notificationHelper.notifyStaff(
                        "Khách hàng đã đặt đơn hàng mới: " + order.getMaDon(),
                        "ORDER", order.getId(),
                        "/admin/don-hang",
                        order.getMaDon()
                );
            } catch (Exception e) {
                // notifyStaff đã log lỗi, không break flow chính
            }

            User finalUser = order.getUser();
            String finalTt = "CHUYEN_KHOAN".equals(order.getPhuongThucTT()) ? "Chuyển khoản" : "VNPAY".equals(order.getPhuongThucTT()) ? "VNPay" : "COD";
            String finalGh = "GHN".equals(order.getShippingCarrier()) ? "Giao Hàng Nhanh" : "Giao Hàng Tiết Kiệm";
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
                } catch (Exception ignored) {
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();

            if ("CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/chuyen-khoan/" + order.getId();
            }
            if ("VNPAY".equals(order.getPhuongThucTT())) {
                String vnpayUrl = orderService.createVNPAYPaymentUrl(order.getId(), httpReq);
                if (vnpayUrl != null) {
                    return "redirect:" + vnpayUrl;
                }
            }
            return "redirect:/checkout/thanh-cong/" + order.getId();
        } catch (RuntimeException e) {
            buildCheckoutModel(model, userId, req.getSelectedIds());
            model.addAttribute("error", e.getMessage());
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

    private void buildCheckoutModel(Model model, Integer userId) {
        buildCheckoutModel(model, userId, null);
    }

    private void buildCheckoutModel(Model model, Integer userId, List<Integer> selectedIds) {
        List<CartItemDTO> cartItems = cartService.getItems(userId);
        if (selectedIds != null && !selectedIds.isEmpty()) {
            Set<Integer> selectedSet = new HashSet<>(selectedIds);
            cartItems = cartItems.stream()
                    .filter(item -> item.getVariantId() != null && selectedSet.contains(item.getVariantId()))
                    .collect(Collectors.toList());
            model.addAttribute("selectedIds", selectedIds);
        }
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDesc(userId);
        BigDecimal subtotal = cartService.total(cartItems);
        BigDecimal phiShip = addresses.isEmpty()
                ? new BigDecimal("10000")
                : multiCarrierShippingService.calculateFeeForCarrier("GHN", addresses.get(0), subtotal);
        BigDecimal tienGiam = calcAutoDiscount(subtotal);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("tienGiam", tienGiam);
        model.addAttribute("tongTam", subtotal.add(phiShip).subtract(tienGiam));
        model.addAttribute("checkoutRequest", new com.duastore.dto.CheckoutRequestDTO());
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("loyaltyBalance", userId != null ? loyaltyPointsService.getBalance(userId) : 0);
        model.addAttribute("loyaltyRedeemRate", loyaltyPointsService.getPointsRedeemRate());
        model.addAttribute("loyaltyEarnRate", loyaltyPointsService.getPointsEarnRate());
        model.addAttribute("userVouchers", userId != null ? voucherWalletService.getAvailableVouchers(userId) : List.of());
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, subtotal);
        model.addAttribute("bestPromo", bestPromo);
        Map<String, String> paymentSettings = siteSettingService.getGroup("payment");
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.get("payment_cod")));
        paymentMethods.put("bank", "1".equals(paymentSettings.get("payment_bank")));
        paymentMethods.put("vnpay", "1".equals(paymentSettings.get("payment_vnpay")));
        model.addAttribute("paymentMethods", paymentMethods);

        Map<String, String> shippingSettings = siteSettingService.getGroup("shipping");
        model.addAttribute("carrierGHNEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghn_enabled", "1")));
        model.addAttribute("carrierGHTKEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghtk_enabled", "1")));
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
            int pointsToRedeem = req.getPointsToRedeem() != null ? req.getPointsToRedeem() : 0;
            Set<Integer> selectedSet = req.getSelectedIds() != null && !req.getSelectedIds().isEmpty()
                    ? new HashSet<>(req.getSelectedIds()) : null;
            Order order = orderService.processCheckout(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu(),
                    pointsToRedeem,
                    selectedSet,
                    req.getShippingCarrier() != null ? req.getShippingCarrier() : "GHN"
            );
            fraudDetectionService.analyzeAndPersist(order);
            try {
                notificationHelper.notifyStaff(
                        "Khách hàng vừa đặt đơn hàng mới: " + order.getMaDon(),
                        "ORDER", order.getId(),
                        "/admin/don-hang/" + order.getId(),
                        order.getMaDon()
                );
            } catch (Exception ignored) {
            }

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
            res.put("message", "Áp dụng mã thành công! Giảm "
                    + (tienGiam.compareTo(BigDecimal.ZERO) > 0
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
    @ResponseBody
    public ResponseEntity<Map<String, Object>> xacNhanChuyenKhoan(@PathVariable Integer id) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                res.put("success", false);
                res.put("message", "Đơn hàng không phải chuyển khoản");
                return ResponseEntity.ok(res);
            }
            if ("DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
                res.put("success", true);
                res.put("alreadyPaid", true);
                res.put("redirectUrl", "/checkout/thanh-cong/" + id);
                return ResponseEntity.ok(res);
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
            } catch (Exception ignored) {
            }

            User finalUser = order.getUser();
            String finalTt2 = "Chuyển khoản";
            String finalGh2 = "EXPRESS".equals(order.getPhuongThucGiaoHang()) ? "Giao hàng nhanh" : "Giao hàng an toàn";
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
                } catch (Exception ignored) {
                }
            });
            emailThread.setDaemon(true);
            emailThread.start();

            res.put("success", true);
            res.put("redirectUrl", "/checkout/thanh-cong/" + id);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.ok(res);
        }
    }

    @GetMapping("/api/check-payment-status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPaymentStatus(@PathVariable Integer id) {
        Map<String, Object> res = new HashMap<>();
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            boolean isPaid = "DA_THANH_TOAN".equals(order.getTrangThaiTT());
            res.put("paid", isPaid);
            if (isPaid) {
                res.put("redirectUrl", "/checkout/thanh-cong/" + id);
            }
        } catch (RuntimeException e) {
            res.put("paid", false);
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/vnpay/return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        Map<String, String> result = vnpayService.verifyReturn(params);
        if (!"true".equals(result.get("success"))) {
            model.addAttribute("error", result.get("message"));
            return "view/client/payment-fail";
        }
        String txnRef = result.get("txnRef");
        if (txnRef != null) {
            try {
                int orderId = Integer.parseInt(txnRef.replace("DUASTORE", ""));
                orderService.updatePaymentStatus(orderId, "DA_THANH_TOAN");
                Order order = orderService.getOrderByUserAndId(getUserId(), orderId);
                model.addAttribute("order", orderService.convertToDTO(order));
            } catch (Exception e) {
                model.addAttribute("error", "Không tìm thấy đơn hàng");
                return "view/client/payment-fail";
            }
        }
        return "redirect:/checkout/thanh-cong/" + txnRef.replace("DUASTORE", "");
    }

    @PostMapping("/vnpay/ipn")
    @ResponseBody
    public ResponseEntity<Map<String, String>> vnpayIPN(@RequestParam Map<String, String> params) {
        Map<String, String> result = vnpayService.verifyReturn(params);
        Map<String, String> response = new HashMap<>();
        if ("true".equals(result.get("success"))
                && "00".equals(result.get("responseCode"))) {
            String txnRef = result.get("txnRef");
            if (txnRef != null) {
                try {
                    int orderId = Integer.parseInt(txnRef.replace("DUASTORE", ""));
                    orderService.updatePaymentStatus(orderId, "DA_THANH_TOAN");
                    response.put("RspCode", "00");
                    response.put("Message", "Confirm Success");
                } catch (Exception e) {
                    response.put("RspCode", "99");
                    response.put("Message", "Order not found");
                }
            } else {
                response.put("RspCode", "99");
                response.put("Message", "Invalid TxnRef");
            }
        } else {
            response.put("RspCode", "99");
            response.put("Message", "Invalid signature");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/thanh-cong/{id}")
    public String orderSuccess(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            OrderDTO dto = orderService.convertToDTO(order);
            model.addAttribute("order", dto);
            model.addAttribute("items", orderService.getOrderItemsByOrder(order));
            model.addAttribute("title", "Đặt hàng thành công");
            return "view/client/order-success";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }
}
