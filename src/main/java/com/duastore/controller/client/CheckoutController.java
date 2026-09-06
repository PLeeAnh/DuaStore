package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.dto.CheckoutRequestDTO;
import com.duastore.dto.OrderDTO;
import com.duastore.model.Address;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.Order;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.model.OrderEventType;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.AsyncEmailService;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.PaymentService;
import com.duastore.service.PricingService;
import com.duastore.dto.CarrierQuote;
import com.duastore.service.MultiCarrierShippingService;
import com.duastore.service.SiteSettingService;
import com.duastore.service.SepayService;
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
/**
 * Controller xử lý các request HTTP liên quan tới thanh toán/đặt hàng (checkout).
 */
public class CheckoutController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckoutController.class);

    // Mã đơn luôn có dạng "DH" + 12 ký tự hex viết hoa (xem OrderService.generateMaDon).
    // SePay tu tach field "code" tu noi dung chuyen khoan theo mau nhan dien rieng cua ho,
    // co the khong nhan ra dinh dang mã đơn tuy chinh cua minh -> can fallback do bằng regex
    // tren field "content" (noi dung chuyen khoan goc, luon chua ma don vi minh tu sinh QR).
    private static final java.util.regex.Pattern MA_DON_PATTERN = java.util.regex.Pattern.compile("DH[0-9A-F]{12}");

    private final OrderService orderService;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final SecurityUtil securityUtil;
    private final AsyncEmailService asyncEmailService;
    private final PaymentService paymentService;
    private final OrderStatusLogService orderStatusLogService;
    private final NotificationHelper notificationHelper;
    private final SepayService sepayService;
    private final FraudDetectionService fraudDetectionService;
    private final VoucherWalletService voucherWalletService;
    private final LoyaltyPointsService loyaltyPointsService;
    private final SiteSettingService siteSettingService;
    private final MultiCarrierShippingService multiCarrierShippingService;
    private final ProductVariantRepository variantRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final PricingService pricingService;

    @Value("${store.latitude}")
    private double storeLat;

    @Value("${store.longitude}")
    private double storeLng;

    public CheckoutController(OrderService orderService, CartService cartService,
            AddressRepository addressRepository,
            PromotionRepository promotionRepository,
            SecurityUtil securityUtil,
            AsyncEmailService asyncEmailService,
            PaymentService paymentService,
            OrderStatusLogService orderStatusLogService,
            NotificationHelper notificationHelper,
            SepayService sepayService,
            FraudDetectionService fraudDetectionService,
            VoucherWalletService voucherWalletService,
            LoyaltyPointsService loyaltyPointsService,
            SiteSettingService siteSettingService,
            MultiCarrierShippingService multiCarrierShippingService,
            ProductVariantRepository variantRepository,
            FlashSaleItemRepository flashSaleItemRepository,
            PricingService pricingService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.securityUtil = securityUtil;
        this.asyncEmailService = asyncEmailService;
        this.paymentService = paymentService;
        this.orderStatusLogService = orderStatusLogService;
        this.notificationHelper = notificationHelper;
        this.sepayService = sepayService;
        this.fraudDetectionService = fraudDetectionService;
        this.voucherWalletService = voucherWalletService;
        this.loyaltyPointsService = loyaltyPointsService;
        this.siteSettingService = siteSettingService;
        this.multiCarrierShippingService = multiCarrierShippingService;
        this.variantRepository = variantRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.pricingService = pricingService;
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

        // Auto-apply best promotion — chỉ chọn mã THỰC SỰ áp dụng được lúc đặt hàng
        // (loại trừ biến thể Flash Sale không cộng dồn được, xem findBestPromo)
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, cartItems, phiShip);
        BigDecimal tienGiam = BigDecimal.ZERO;
        String autoPromoCode = null;
        if (bestPromo != null) {
            BigDecimal eligibleAmount = orderService.resolveEligibleAmountForCart(bestPromo, cartItems, Map.of());
            tienGiam = orderService.calculateDiscount(bestPromo, eligibleAmount, phiShip);
            autoPromoCode = bestPromo.getMaCode();
            // Calculate per-item discounted prices — bỏ qua item Flash Sale vì mã này
            // không cộng dồn được với giá Flash Sale (giữ nguyên giá Flash Sale hiển thị)
            for (CartItemDTO item : cartItems) {
                boolean isFlashExcluded = "FLASH_SALE".equals(item.getNguonGia())
                        && !Boolean.TRUE.equals(bestPromo.getStackable());
                if (item.getGiaBan() != null && !isFlashExcluded) {
                    if ("PHAN_TRAM".equals(bestPromo.getLoaiGiam())) {
                        BigDecimal discountPct = bestPromo.getGiaTriGiam();
                        BigDecimal discountedPrice = item.getGiaBan()
                                .multiply(BigDecimal.valueOf(100).subtract(discountPct))
                                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        item.setGiaBanSauGiam(discountedPrice);
                    } else {
                        // For fixed-amount, apply proportional discount
                        if (tienGiam.compareTo(BigDecimal.ZERO) > 0 && eligibleAmount.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal ratio = item.getGiaBan().multiply(tienGiam).divide(eligibleAmount, java.math.RoundingMode.HALF_UP);
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
        model.addAttribute("bodyClass", "ds-checkout-page");
        model.addAttribute("userVouchers", userId != null ? voucherWalletService.getAvailableVouchers(userId) : List.of());
        model.addAttribute("loyaltyBalance", userId != null ? loyaltyPointsService.getBalance(userId) : 0);
        model.addAttribute("loyaltyRedeemRate", loyaltyPointsService.getPointsRedeemRate());
        model.addAttribute("loyaltyEarnRate", loyaltyPointsService.getPointsEarnRate());

        // Bank info for transfer payment
        model.addAttribute("bankInfo", paymentService.getBankInfo());

        // Estimated delivery date
        model.addAttribute("estimatedDeliveryDate", java.time.LocalDate.now().plusDays(5).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " – " + java.time.LocalDate.now().plusDays(10).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Load payment method toggles from DB
        Map<String, String> paymentSettings = siteSettingService.getGroup("payment");
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.getOrDefault("payment_cod", "1")));
        paymentMethods.put("bank", "1".equals(paymentSettings.getOrDefault("payment_bank", "1")));
        paymentMethods.put("sepay", "1".equals(paymentSettings.getOrDefault("payment_sepay", "0")));
        model.addAttribute("paymentMethods", paymentMethods);

        // Load carrier settings
        Map<String, String> shippingSettings = siteSettingService.getGroup("shipping");
        model.addAttribute("carrierGHNEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghn_enabled", "1")));
        model.addAttribute("carrierGHTKEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghtk_enabled", "1")));

        String freeMin = shippingSettings.getOrDefault("shipping_free_min", "500000");
        model.addAttribute("shippingFreeMin", new BigDecimal(freeMin.isBlank() ? "500000" : freeMin.trim()));

        model.addAttribute("storeLat", storeLat);
        model.addAttribute("storeLng", storeLng);

        // Set default payment method based on available methods
        if (!paymentMethods.get("cod") && paymentMethods.get("sepay")) {
            model.addAttribute("defaultPaymentMethod", "SEPAY_QR");
        } else if (!paymentMethods.get("cod") && paymentMethods.get("bank")) {
            model.addAttribute("defaultPaymentMethod", "CHUYEN_KHOAN");
        } else {
            model.addAttribute("defaultPaymentMethod", "COD");
        }
        return "view/client/checkout";
    }

    private void addPaymentMethodsToModel(Model model, Map<String, String> paymentSettings) {
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.getOrDefault("payment_cod", "1")));
        paymentMethods.put("bank", "1".equals(paymentSettings.getOrDefault("payment_bank", "1")));
        paymentMethods.put("sepay", "1".equals(paymentSettings.getOrDefault("payment_sepay", "0")));
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

        validateCheckoutRequest(req);
        try {
            Set<Integer> selectedSet = req.getSelectedIds() != null && !req.getSelectedIds().isEmpty()
                    ? new HashSet<>(req.getSelectedIds()) : null;
            Order order = orderService.processCheckoutIdempotent(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu(),
                    req.getPointsToRedeem() != null ? req.getPointsToRedeem() : 0,
                    selectedSet,
                    req.getShippingCarrier() != null ? req.getShippingCarrier() : "GHN",
                    req.getIdempotencyKey()
            );

            fraudDetectionService.analyzeAndPersist(order);

            try {
                notificationHelper.notifyStaff(
                        "Khách hàng đã đặt đơn hàng mới: " + order.getMaDon(),
                        "ORDER", order.getId(),
                        "/admin/don-hang",
                        order.getMaDon(),
                        com.duastore.config.security.PermissionEnum.ORDER_READ
                );
            } catch (Exception e) {
                // notifyStaff đã log lỗi, không break flow chính
            }

            // Email xac nhan gui BAT DONG BO (best-effort) — khong bao gio chan/khong tao
            // tinh huong "guu don thanh cong nhung vi email loi nen bi huy don" nua.
            // Bo qua CHUYEN_KHOAN va SEPAY_QR: 2 don nay chua thanh toan luc tao, email
            // chi gui 1 lan khi thanh toan thuc su duoc xac nhan.
            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT()) && !"SEPAY_QR".equals(order.getPhuongThucTT())) {
                asyncEmailService.sendOrderSuccess(order);
            }
            asyncEmailService.notifyStaffNewOrder(order);

            if ("CHUYEN_KHOAN".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/chuyen-khoan/" + order.getId();
            }
            if ("SEPAY_QR".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/sepay/qr/" + order.getId();
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

    private BigDecimal calcAutoDiscount(List<CartItemDTO> cartItems, BigDecimal phiShip) {
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, cartItems, phiShip);
        if (bestPromo != null) {
            BigDecimal eligible = orderService.resolveEligibleAmountForCart(bestPromo, cartItems, Map.of());
            return orderService.calculateDiscount(bestPromo, eligible, phiShip);
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
        BigDecimal tienGiam = calcAutoDiscount(cartItems, phiShip);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("addresses", addresses);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("tienGiam", tienGiam);
        model.addAttribute("tongTam", subtotal.add(phiShip).subtract(tienGiam));
        model.addAttribute("checkoutRequest", new com.duastore.dto.CheckoutRequestDTO());
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("bodyClass", "ds-checkout-page");
        model.addAttribute("loyaltyBalance", userId != null ? loyaltyPointsService.getBalance(userId) : 0);
        model.addAttribute("loyaltyRedeemRate", loyaltyPointsService.getPointsRedeemRate());
        model.addAttribute("loyaltyEarnRate", loyaltyPointsService.getPointsEarnRate());
        model.addAttribute("userVouchers", userId != null ? voucherWalletService.getAvailableVouchers(userId) : List.of());
        model.addAttribute("bankInfo", paymentService.getBankInfo());
        List<Promotion> activePromotions = getActivePromotions();
        Promotion bestPromo = findBestPromo(activePromotions, cartItems, phiShip);
        model.addAttribute("bestPromo", bestPromo);
        Map<String, String> paymentSettings = siteSettingService.getGroup("payment");
        Map<String, Boolean> paymentMethods = new HashMap<>();
        paymentMethods.put("cod", "1".equals(paymentSettings.getOrDefault("payment_cod", "1")));
        paymentMethods.put("bank", "1".equals(paymentSettings.getOrDefault("payment_bank", "1")));
        paymentMethods.put("sepay", "1".equals(paymentSettings.getOrDefault("payment_sepay", "0")));
        model.addAttribute("paymentMethods", paymentMethods);

        Map<String, String> shippingSettings = siteSettingService.getGroup("shipping");
        model.addAttribute("carrierGHNEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghn_enabled", "1")));
        model.addAttribute("carrierGHTKEnabled", "1".equals(shippingSettings.getOrDefault("carrier_ghtk_enabled", "1")));
    }

    /**
     * Chỉ tự động chọn khuyến mãi nếu nó THỰC SỰ áp dụng được khi đặt hàng — tính theo
     * "số tiền đủ điều kiện" (loại trừ biến thể đang Flash Sale không cộng dồn được,
     * giống hệt logic ở OrderService.resolveEligibleAmount) thay vì cả subtotal thô,
     * để tránh trường hợp trang xem trước gợi ý 1 mã nhưng lúc đặt hàng lại bị từ chối.
     */
    private Promotion findBestPromo(List<Promotion> promos, List<CartItemDTO> cartItems, BigDecimal phiShip) {
        BigDecimal maxPct = new BigDecimal("100");
        BigDecimal ship = phiShip != null ? phiShip : BigDecimal.ZERO;
        Map<Integer, BigDecimal> eligibleByPromo = new HashMap<>();
        for (Promotion p : promos) {
            eligibleByPromo.put(p.getId(),
                    orderService.resolveEligibleAmountForCart(p, cartItems, Map.of()));
        }
        return promos.stream()
                .filter(p -> eligibleByPromo.get(p.getId()).compareTo(BigDecimal.ZERO) > 0)
                .filter(p -> p.getDonHangToiThieu() == null
                        || eligibleByPromo.get(p.getId()).compareTo(p.getDonHangToiThieu()) >= 0)
                .filter(p -> !"PHAN_TRAM".equals(p.getLoaiGiam()) || p.getGiaTriGiam().compareTo(maxPct) <= 0)
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(p -> orderService.calculateDiscount(p, eligibleByPromo.get(p.getId()), ship)))
                .orElse(null);
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiCreateOrder(@Valid @ModelAttribute("checkoutRequest") CheckoutRequestDTO req,
            BindingResult result, jakarta.servlet.http.HttpServletRequest request) {
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
        validateCheckoutRequest(req);
        try {
            int pointsToRedeem = req.getPointsToRedeem() != null ? req.getPointsToRedeem() : 0;
            Set<Integer> selectedSet = req.getSelectedIds() != null && !req.getSelectedIds().isEmpty()
                    ? new HashSet<>(req.getSelectedIds()) : null;
            Order order = orderService.processCheckoutIdempotent(
                    userId, req.getAddressId(), req.getPhuongThucTT(),
                    req.getPhuongThucGiaoHang(), req.getMaCode(), req.getGhiChu(),
                    pointsToRedeem,
                    selectedSet,
                    req.getShippingCarrier() != null ? req.getShippingCarrier() : "GHN",
                    req.getIdempotencyKey()
            );
            fraudDetectionService.analyzeAndPersist(order);
            try {
                notificationHelper.notifyStaff(
                        "Khách hàng vừa đặt đơn hàng mới: " + order.getMaDon(),
                        "ORDER", order.getId(),
                        "/admin/don-hang/" + order.getId(),
                        order.getMaDon(),
                        com.duastore.config.security.PermissionEnum.ORDER_READ
                );
            } catch (Exception ignored) {
            }

            // Email xac nhan gui BAT DONG BO (best-effort) — khong bao gio chan/khong tao
            // tinh huong "gui don thanh cong nhung vi email loi nen bi huy don". Bo qua
            // CHUYEN_KHOAN va SEPAY_QR vi 2 don nay chua thanh toan luc tao — email chi gui
            // 1 lan khi thanh toan thuc su duoc xac nhan (xem xacNhanChuyenKhoan / SePay IPN).
            if (!"CHUYEN_KHOAN".equals(order.getPhuongThucTT()) && !"SEPAY_QR".equals(order.getPhuongThucTT())) {
                asyncEmailService.sendOrderSuccess(order);
            }
            asyncEmailService.notifyStaffNewOrder(order);

            res.put("success", true);
            res.put("orderId", order.getId());
            res.put("maDon", order.getMaDon());

            String paymentMethod = req.getPhuongThucTT();
            if ("SEPAY_QR".equals(paymentMethod)) {
                res.put("redirectType", "SEPAY_QR");
                res.put("redirectUrl", "/checkout/sepay/qr/" + order.getId());
            } else if ("CHUYEN_KHOAN".equals(paymentMethod)) {
                res.put("redirectType", "CHUYEN_KHOAN");
                res.put("redirectUrl", "/checkout/chuyen-khoan/" + order.getId());
            } else {
                res.put("redirectType", "SUCCESS");
                res.put("redirectUrl", "/checkout/thanh-cong/" + order.getId());
            }
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
        String customQr = paymentService.getQrUrl();
        if (customQr != null && !customQr.isBlank()) {
            res.put("qrUrl", customQr);
        } else {
            res.put("qrUrl", paymentService.generateVietQrUrl("DUASTORE", amount));
        }
        res.put("accountNumber", paymentService.getAccountNumber());
        res.put("accountName", paymentService.getAccountName());
        res.put("bankName", paymentService.getBankName());
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
            // Tính theo số tiền THỰC SỰ đủ điều kiện (loại trừ biến thể Flash Sale không
            // cộng dồn được) — giống hệt kiểm tra lúc đặt hàng thật, để không báo "áp dụng
            // thành công" ở đây rồi lại bị từ chối khi bấm Đặt hàng.
            Integer userId = getUserId();
            List<CartItemDTO> cartItems = userId != null ? cartService.getItems(userId) : List.of();
            BigDecimal eligibleAmount = cartItems.isEmpty()
                    ? subtotal
                    : orderService.resolveEligibleAmountForCart(promo, cartItems, Map.of());
            orderService.validatePromotion(promo, eligibleAmount);
            BigDecimal tienGiam = orderService.calculateDiscount(promo, eligibleAmount);
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
            model.addAttribute("bankName", paymentService.getBankName());
            model.addAttribute("title", "Thanh toán chuyển khoản");
            model.addAttribute("bodyClass", "ds-checkout-page");
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

            // Idempotent + loo PESSIMISTIC_WRITE: goi nhieu lan cung key chi xac nhan 1 lan.
            boolean firstTime = orderService.confirmPaid(id);
            if (firstTime) {
                asyncEmailService.sendOrderSuccess(order);
                asyncEmailService.notifyStaffNewOrder(order);
                try {
                    notificationHelper.notifyStaff(
                            "Khách hàng đã xác nhận thanh toán cho đơn hàng: " + order.getMaDon(),
                            "ORDER", order.getId(),
                            "/admin/don-hang/" + order.getId(),
                            order.getMaDon(),
                            com.duastore.config.security.PermissionEnum.ORDER_READ
                    );
                } catch (Exception ignored) {
                }
            }

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

    @GetMapping("/sepay/qr/{id}")
    public String sepayQR(@PathVariable Integer id, Model model) {
        Integer userId = getUserId();
        try {
            Order order = orderService.getOrderByUserAndId(userId, id);
            if (!"SEPAY_QR".equals(order.getPhuongThucTT())) {
                return "redirect:/checkout/thanh-cong/" + id;
            }
            if ("DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
                return "redirect:/checkout/thanh-cong/" + id;
            }
            String qrUrl = sepayService.generateQrUrl(
                    order.getTongThanhToan().longValue(),
                    order.getMaDon());
            model.addAttribute("order", orderService.convertToDTO(order));
            model.addAttribute("qrUrl", qrUrl);
            model.addAttribute("bankName", sepayService.getBankName());
            model.addAttribute("accountNumber", sepayService.getBankAccount());
            model.addAttribute("accountName", sepayService.getBankHolder());
            model.addAttribute("title", "Thanh toán QR (VietQR)");
            model.addAttribute("bodyClass", "ds-checkout-page");
            return "view/client/sepay-qr";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }

    @PostMapping("/sepay/ipn")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sepayIPN(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Map<String, Object> response = new HashMap<>();
        if (!sepayService.verifyApiKey(authorization)) {
            response.put("success", false);
            response.put("message", "Invalid API key");
            return ResponseEntity.status(401).body(response);
        }
        try {
            String transferType = (String) body.getOrDefault("transferType", "");
            log.info("SePay IPN nhan duoc: transferType={}, code={}, content={}, transferAmount={}",
                    transferType, body.get("code"), body.get("content"), body.get("transferAmount"));
            if (!"in".equals(transferType)) {
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
            Number amountNum = (Number) body.get("transferAmount");
            long transferAmount = amountNum != null ? amountNum.longValue() : 0;
            String code = (String) body.getOrDefault("code", "");
            String content = (String) body.getOrDefault("content", "");

            Order order = null;
            if (code != null && !code.isBlank()) {
                try {
                    order = orderService.getOrderByMaDon(code);
                } catch (RuntimeException e) {
                    order = null;
                }
            }
            if (order == null && content != null && !content.isBlank()) {
                java.util.regex.Matcher m = MA_DON_PATTERN.matcher(content.toUpperCase());
                if (m.find()) {
                    String extractedMaDon = m.group();
                    log.info("SePay IPN: field 'code' khong khop don nao, thu bang ma don trich tu 'content': {}", extractedMaDon);
                    try {
                        order = orderService.getOrderByMaDon(extractedMaDon);
                    } catch (RuntimeException e) {
                        order = null;
                    }
                }
            }
            if (order == null) {
                log.warn("SePay IPN: khong tim thay don hang tu code='{}' hay content='{}'", code, content);
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.ok(response);
            }
            if (!"SEPAY_QR".equals(order.getPhuongThucTT())) {
                response.put("success", false);
                response.put("message", "Order is not SEPAY_QR type");
                return ResponseEntity.ok(response);
            }
            if ("DA_HUY".equals(order.getTrangThaiDon())) {
                response.put("success", false);
                response.put("message", "Order cancelled");
                return ResponseEntity.ok(response);
            }
            if ("DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
            if (transferAmount < order.getTongThanhToan().longValue()) {
                response.put("success", false);
                response.put("message", "Amount mismatch");
                return ResponseEntity.ok(response);
            }
            boolean justPaid = orderService.updatePaymentStatus(order.getId(), "DA_THANH_TOAN");
            if (justPaid) {
                orderStatusLogService.ghiLog(order, OrderEventType.PAYMENT_CONFIRMED, null, null, null, null);
                log.info("SePay IPN: xac nhan thanh toan thanh cong cho don {}", order.getMaDon());
                try {
                    asyncEmailService.sendOrderSuccess(order);
                } catch (Exception ignored) {
                }
            } else {
                log.info("SePay IPN: don {} da duoc xu ly boi request truoc do (bo qua trung lap)", order.getMaDon());
            }
            response.put("success", true);
        } catch (Exception e) {
            log.error("SePay IPN: loi khi xu ly webhook", e);
            response.put("success", false);
            response.put("message", e.getMessage());
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

    private void validateCheckoutRequest(CheckoutRequestDTO req) {
        Map<String, String> shippingSettings = siteSettingService.getGroup("shipping");
        String carrier = req.getShippingCarrier() != null ? req.getShippingCarrier() : "GHN";
        boolean carrierEnabled = "1".equals(shippingSettings.getOrDefault("carrier_" + carrier.toLowerCase() + "_enabled", "1"));
        if (!carrierEnabled) {
            throw new RuntimeException("Đơn vị vận chuyển " + carrier + " hiện đang tạm tắt");
        }
        Map<String, String> paymentSettings = siteSettingService.getGroup("payment");
        String paymentMethod = req.getPhuongThucTT() != null ? req.getPhuongThucTT() : "";
        boolean paymentEnabled = switch (paymentMethod) {
            case "COD" -> "1".equals(paymentSettings.getOrDefault("payment_cod", "1"));
            case "CHUYEN_KHOAN" -> "1".equals(paymentSettings.getOrDefault("payment_bank", "1"));
            case "SEPAY_QR" -> {
                boolean enabled = "1".equals(paymentSettings.getOrDefault("payment_sepay", "0"));
                if (!enabled) {
                    throw new RuntimeException("Thanh toán QR (VietQR) đã tắt. Bật tại Admin → Cấu hình thanh toán.");
                }
                if (!sepayService.isConfigured()) {
                    throw new RuntimeException("Sepay chưa cấu hình Merchant ID / Secret Key. Nhập tại Admin → Cấu hình thanh toán → Cấu hình Sepay.");
                }
                yield true;
            }
            default -> false;
        };
        if (!paymentEnabled) {
            throw new RuntimeException("Phương thức thanh toán " + paymentMethod + " hiện không khả dụng");
        }

        // Validate selected variant IDs
        if (req.getSelectedIds() != null && !req.getSelectedIds().isEmpty()) {
            List<Integer> variantIds = req.getSelectedIds();
            // Check if variants exist, are active, and have stock
            List<ProductVariant> variants = variantRepository.findAllById(variantIds);
            if (variants.size() != variantIds.size()) {
                throw new RuntimeException("Một hoặc nhiều biến thể sản phẩm không tồn tại");
            }
            for (ProductVariant variant : variants) {
                if (!Boolean.TRUE.equals(variant.isActive())) {
                    throw new RuntimeException("Sản phẩm biến thể " + variant.getTenBienThe() + " không còn hoạt động");
                }
                if (variant.getSoLuongTon() == null || variant.getSoLuongTon() <= 0) {
                    throw new RuntimeException("Sản phẩm " + variant.getTenBienThe() + " đã hết hàng");
                }
                // Check flash sale quota if applicable
                FlashSaleItem flashItem = flashSaleItemRepository.findBestActiveByVariantId(variant.getId(), LocalDateTime.now())
                        .orElse(null);
                if (flashItem != null) {
                    if (!pricingService.hasRemainingQuota(flashItem)) {
                        throw new RuntimeException("Sản phẩm " + variant.getTenBienThe() + " đã hết suất Flash Sale");
                    }
                }
            }
        }
    }
}

