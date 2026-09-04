package com.duastore.service.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.TimelineEvent;
import com.duastore.model.*;
import com.duastore.repository.*;
import com.duastore.service.AsyncEmailService;
import com.duastore.service.GHNShippingService;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.MultiCarrierShippingService;
import com.duastore.service.PricingService;
import com.duastore.service.SepayService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.util.PriceUtils;
import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
/**
 * Service chứa nghiệp vụ (business logic) xử lý đơn hàng.
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderStatusLogService orderStatusLogService;
    private final UserVoucherRepository userVoucherRepository;
    private final GHNShippingService ghnShippingService;
    private final SepayService sepayService;
    private final PricingService pricingService;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final LoyaltyPointsService loyaltyPointsService;
    private final MultiCarrierShippingService multiCarrierShippingService;
    private final CheckoutIdempotencyService checkoutIdempotencyService;
    private final AsyncEmailService asyncEmailService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            CartService cartService, AddressRepository addressRepository,
            PromotionRepository promotionRepository, UserRepository userRepository,
            CartItemRepository cartItemRepository,
            OrderAssignmentRepository orderAssignmentRepository,
            ProductVariantRepository variantRepository,
            OrderStatusLogService orderStatusLogService,
            UserVoucherRepository userVoucherRepository,
            GHNShippingService ghnShippingService,
            SepayService sepayService,
            PricingService pricingService,
            FlashSaleRepository flashSaleRepository,
            FlashSaleItemRepository flashSaleItemRepository,
            LoyaltyPointsService loyaltyPointsService,
            MultiCarrierShippingService multiCarrierShippingService,
            CheckoutIdempotencyService checkoutIdempotencyService,
            AsyncEmailService asyncEmailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
        this.variantRepository = variantRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.userVoucherRepository = userVoucherRepository;
        this.ghnShippingService = ghnShippingService;
        this.sepayService = sepayService;
        this.pricingService = pricingService;
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.loyaltyPointsService = loyaltyPointsService;
        this.multiCarrierShippingService = multiCarrierShippingService;
        this.checkoutIdempotencyService = checkoutIdempotencyService;
        this.asyncEmailService = asyncEmailService;
    }

    /**
     * Dat hang co idempotency key — chong tao don trung khi khach bam submit nhieu lan
     * hoac mang chan rui bam lai. Cung key se chi tao 1 don duy nhat.
     */
    @Transactional
    public Order processCheckoutIdempotent(Integer userId, Integer addressId, String phuongThucTT,
            String phuongThucGiaoHang, String maCode, String ghiChu, int pointsToRedeem,
            Set<Integer> selectedVariantIds, String shippingCarrier, String idempotencyKey) {
        return checkoutIdempotencyService.execute(idempotencyKey,
                () -> processCheckout(userId, addressId, phuongThucTT, phuongThucGiaoHang,
                        maCode, ghiChu, pointsToRedeem, selectedVariantIds, shippingCarrier));
    }

    /**
     * Xac nhan don hang da thanh toan — IDEMPOTENT bang UPDATE nguyen tu (xem
     * OrderRepository.markPaidIfUnpaid).
     *
     * Return true neu lan DAU tien chuyen CHUA_THANH_TOAN -> DA_THANH_TOAN (co log).
     * Return false neu don da duoc thanh toan truoc do hoac khong hop le — goi lai an toan.
     * Dieu nay giai quyet tinh huong khach bam "Đã thanh toán" nhieu lan ma chi chay 1 lan.
     */
    @Transactional
    public boolean confirmPaid(Integer orderId) {
        int updated = orderRepository.markPaidIfUnpaid(orderId);
        if (updated == 0) {
            return false;
        }
        Order order = orderRepository.findByIdWithUserAndItems(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        orderStatusLogService.ghiLog(order, OrderEventType.PAYMENT_CONFIRMED, null, null, null, null);
        clearCartItemsForOrder(order);
        autoAssignAndNotify(order);
        return true;
    }

    /**
     * Tu dong phan cong don cho 1 ADMIN/STAFF bat ky ngay khi thanh toan thanh cong, va
     * gui email bao cho nguoi duoc phan cong (dat hang + thanh toan thanh cong, can xu ly).
     * Bo qua neu don da duoc phan cong tu truoc (tranh gan lai/gui trung khi payment status
     * duoc goi lai nhieu lan). Best-effort — loi o day khong duoc lam hong luong thanh toan.
     */
    private void autoAssignAndNotify(Order order) {
        try {
            if (orderAssignmentRepository.findByOrderId(order.getId()).isPresent()) {
                return;
            }
            List<User> pool = userRepository.findByRolesNameIn(List.of("ADMIN", "STAFF"));
            if (pool.isEmpty()) {
                return;
            }
            User picked = pool.get(new java.util.Random().nextInt(pool.size()));
            OrderAssignment assignment = new OrderAssignment();
            assignment.setOrder(order);
            assignment.setAdmin(picked);
            assignment.setTrangThai("DANG_XU_LY");
            orderAssignmentRepository.save(assignment);
            asyncEmailService.sendOrderAssigned(order, picked,
                    "Hệ thống (tự động — đơn đã đặt và thanh toán thành công)");
        } catch (Exception e) {
            // best-effort, khong anh huong luong thanh toan chinh
        }
    }

    /**
     * Gui email cam on + moi danh gia san pham khi don chuyen sang "Da hoan thanh".
     * Goi tu ca luong khach tu bam "Da nhan duoc hang" (markOrderReceived) lan luong
     * admin cap nhat trang thai thu cong (xem AdminOrderService).
     */
    public void notifyOrderCompleted(Order order) {
        if (order == null || order.getUser() == null || order.getUser().getEmail() == null) {
            return;
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        asyncEmailService.sendOrderCompleted(order.getUser().getEmail(), order.getUser().getHoTen(),
                order.getMaDon(), items);
    }

    /**
     * Xoa cac dong trong gio hang cua khach TRUNG voi cac variant cua don nay, goi luc
     * thanh toan CHUYEN_KHOAN/SEPAY_QR duoc xac nhan that (xem processCheckout — 2
     * phuong thuc nay khong xoa gio hang ngay luc dat don nua). Chi xoa dung nhung dong
     * lien quan don, khong dong toi cac san pham khac khach da them vao gio sau do.
     */
    private void clearCartItemsForOrder(Order order) {
        if (order.getUser() == null) {
            return;
        }
        Integer userId = order.getUser().getId();
        Set<Integer> variantIds = order.getOrderItems().stream()
                .map(OrderItem::getVariantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (variantIds.isEmpty()) {
            return;
        }
        List<CartItem> toRemove = cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .filter(ci -> variantIds.contains(ci.getVariantId()))
                .toList();
        if (!toRemove.isEmpty()) {
            cartItemRepository.deleteAll(toRemove);
        }
    }

    @Transactional
    public Order processCheckout(Integer userId, Integer addressId, String phuongThucTT,
            String phuongThucGiaoHang, String maCode, String ghiChu) {
        return processCheckout(userId, addressId, phuongThucTT, phuongThucGiaoHang, maCode, ghiChu, 0, null, "GHN");
    }

    @Transactional
    public Order processCheckout(Integer userId, Integer addressId, String phuongThucTT,
            String phuongThucGiaoHang, String maCode, String ghiChu, int pointsToRedeem) {
        return processCheckout(userId, addressId, phuongThucTT, phuongThucGiaoHang, maCode, ghiChu, pointsToRedeem, null, "GHN");
    }

    @Transactional
    public Order processCheckout(Integer userId, Integer addressId, String phuongThucTT,
            String phuongThucGiaoHang, String maCode, String ghiChu, int pointsToRedeem, Set<Integer> selectedVariantIds) {
        return processCheckout(userId, addressId, phuongThucTT, phuongThucGiaoHang, maCode, ghiChu, pointsToRedeem, selectedVariantIds, "GHN");
    }

    @Transactional
    public Order processCheckout(Integer userId, Integer addressId, String phuongThucTT,
            String phuongThucGiaoHang, String maCode, String ghiChu, int pointsToRedeem,
            Set<Integer> selectedVariantIds, String shippingCarrier) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không hợp lệ");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByNgayThemDesc(userId);
        if (selectedVariantIds != null && !selectedVariantIds.isEmpty()) {
            cartItems = cartItems.stream()
                    .filter(ci -> ci.getVariantId() != null && selectedVariantIds.contains(ci.getVariantId()))
                    .collect(Collectors.toList());
        }
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // Load flash sale item map (by variant) once for all items
        Map<Integer, FlashSaleItem> flashItemMap = pricingService.loadActiveFlashSaleItemMap(
                cartItems.stream().map(CartItem::getVariantId).collect(Collectors.toList()));

        Order order = new Order();
        order.setMaDon(generateMaDon());
        order.setUser(user);
        order.setAddressId(addressId);
        order.setSnapTenNguoiNhan(address.getTenNguoiNhan());
        order.setSnapSoDienThoai(address.getSoDienThoai());
        order.setSnapDiaChi(buildFullAddress(address));
        order.setPhuongThucTT(phuongThucTT);
        order.setPhuongThucGiaoHang("SHIP");
        order.setGhiChu(ghiChu);
        order.setShippingCarrier(shippingCarrier);

        BigDecimal tienHang = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            ProductVariant variant = ci.getVariant();
            if (product == null || variant == null) {
                throw new RuntimeException("Một sản phẩm trong giỏ hàng đã bị xóa. Vui lòng cập nhật giỏ hàng trước khi đặt.");
            }
            FlashSaleItem flashItem = flashItemMap.get(variant.getId());
            PricingService.PriceResult priced = pricingService.resolvePrice(variant, flashItem);
            BigDecimal donGia = priced.finalPrice();
            String loaiGia = priced.source().name();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(ci.getSoLuong()));
            tienHang = tienHang.add(thanhTien);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setVariantId(variant.getId());
            orderItem.setTenSanPham(product.getTenSanPham());
            orderItem.setTenBienThe(variant.getTenBienThe());
            orderItem.setHinhAnhSP(variant.getHinhAnh() != null ? variant.getHinhAnh() : product.getHinhAnhChinh());
            orderItem.setDonGia(donGia);
            orderItem.setSoLuong(ci.getSoLuong());
            orderItem.setThanhTien(thanhTien);
            orderItem.setLoaiGia(loaiGia);
            orderItem.setGiaVon(variant.getGiaVon());
            order.getOrderItems().add(orderItem);
        }
        order.setTienHang(tienHang);
        order.setPhiVanChuyen(calculateShipFee(address, shippingCarrier, tienHang));

        if (maCode != null && !maCode.isBlank()) {
            Promotion promo = promotionRepository.findByMaCodeIgnoreCaseAndIsActiveTrue(maCode.trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá \"" + maCode + "\" không tồn tại hoặc đã bị vô hiệu hóa"));
            Promotion lockedPromo = promotionRepository.findByIdWithLock(promo.getId())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

            // Voucher trong ví: không bắt buộc phải lưu vào ví trước khi dùng
            UserVoucher userVoucher = userVoucherRepository.findByUserIdAndPromotionId(userId, lockedPromo.getId())
                    .orElse(null);
            if (userVoucher != null) {
                if (userVoucher.getStatus() != VoucherStatus.AVAILABLE) {
                    throw new RuntimeException("Voucher này không còn sử dụng được");
                }
                if (userVoucher.getExpiredAt() != null && userVoucher.getExpiredAt().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Voucher đã hết hạn");
                }
                if (userVoucher.getRemainingUses() == null || userVoucher.getRemainingUses() <= 0) {
                    throw new RuntimeException("Voucher đã hết lượt sử dụng");
                }
            }

            Map<Integer, Product> productById = cartItems.stream()
                    .map(CartItem::getProduct)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
            BigDecimal eligibleAmount = resolveEligibleAmount(lockedPromo, order.getOrderItems(), productById);
            // Mã không áp dụng được cho BẤT KỲ sản phẩm nào trong đơn (vd: toàn bộ đơn là
            // hàng Flash Sale không cộng dồn được mã, hoặc không sản phẩm nào thuộc phạm vi
            // mã) — bỏ qua mã một cách âm thầm và vẫn cho đặt hàng bình thường theo giá gốc,
            // thay vì chặn cả đơn hàng vì 1 mã (thường là do hệ thống tự động gợi ý) không
            // dùng được. Nếu số tiền đủ điều kiện > 0 nhưng chưa đạt mức tối thiểu thì vẫn
            // báo lỗi rõ ràng như cũ để khách biết cần mua thêm bao nhiêu mới áp dụng được.
            if (eligibleAmount.compareTo(BigDecimal.ZERO) > 0) {
                // validatePromotion la kiem tra "mem" cho phan lon dieu kien (con han, du dieu
                // kien don toi thieu...) — rieng gioi han soLanDung/budget duoc thuc thi THAT SU
                // nguyen tu o claimUsageIfAvailable ben duoi, khong con dua vao doc-roi-ghi nua.
                validatePromotion(lockedPromo, eligibleAmount);
                BigDecimal tienGiam = calculateDiscount(lockedPromo, eligibleAmount,
                        order.getPhiVanChuyen() != null ? order.getPhiVanChuyen() : BigDecimal.ZERO);
                if (promotionRepository.claimUsageIfAvailable(lockedPromo.getId(), tienGiam) == 0) {
                    throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng hoặc hết ngân sách");
                }
                order.setTienGiam(tienGiam);
                order.setPromotion(lockedPromo);

                if (userVoucher != null) {
                    // Nguyen tu — chong khach double-submit checkout lam voucher bi tru
                    // luot 2 lan (xem UserVoucherRepository.consumeUseIfAvailable).
                    int consumed = userVoucherRepository.consumeUseIfAvailable(
                            userVoucher.getId(), tienGiam, LocalDateTime.now());
                    if (consumed == 0) {
                        throw new RuntimeException("Voucher đã hết lượt sử dụng");
                    }
                }
            }
        }

        if (order.getTienGiam() == null) {
            order.setTienGiam(BigDecimal.ZERO);
        }

        BigDecimal pointsDiscount = BigDecimal.ZERO;
        StringBuilder pointsNote = new StringBuilder();
        if (pointsToRedeem > 0) {
            int balance = loyaltyPointsService.getBalance(userId);
            if (pointsToRedeem > balance) {
                throw new RuntimeException("Bạn chỉ có " + balance + " điểm tích lũy (cần " + pointsToRedeem + ")");
            }
            int maxPoints = loyaltyPointsService.getPointsEarnRate() * 100;
            if (pointsToRedeem > maxPoints) {
                throw new RuntimeException("Chỉ có thể dùng tối đa " + maxPoints + " điểm cho một đơn hàng");
            }
            BigDecimal redeemValue = loyaltyPointsService.convertPointsToMoney(pointsToRedeem);
            BigDecimal tongTruocGiam = order.getTienHang().add(order.getPhiVanChuyen()).subtract(order.getTienGiam());
            if (redeemValue.compareTo(tongTruocGiam) > 0) {
                redeemValue = tongTruocGiam;
                pointsToRedeem = redeemValue.divideToIntegralValue(BigDecimal.valueOf(loyaltyPointsService.getPointsRedeemRate())).intValue();
                if (pointsToRedeem <= 0) {
                    throw new RuntimeException("Số điểm không phù hợp");
                }
                redeemValue = loyaltyPointsService.convertPointsToMoney(pointsToRedeem);
            }
            pointsDiscount = redeemValue;
            pointsNote.append(" (dùng ").append(pointsToRedeem).append(" điểm, giảm ").append(PriceUtils.format(redeemValue)).append(")");
        }

        BigDecimal tong = order.getTienHang().add(order.getPhiVanChuyen()).subtract(order.getTienGiam()).subtract(pointsDiscount);
        if (tong.compareTo(BigDecimal.ZERO) < 0) {
            tong = BigDecimal.ZERO;
        }
        order.setTongThanhToan(tong);
        if (ghiChu != null && !ghiChu.isBlank()) {
            ghiChu = ghiChu + pointsNote.toString();
        } else if (pointsNote.length() > 0) {
            ghiChu = pointsNote.toString().trim();
        }
        order.setGhiChu(ghiChu);

        // Lock flash sale + kiem tra con hang (KHONG tru ton kho o day — ton kho thuc
        // te chi bi tru khi don chuyen sang trang thai "Dang giao", xem
        // AdminOrderService.adjustStock — tranh tru nham hang khi don con dang cho
        // xac nhan hoac bi huy truoc khi giao).
        for (CartItem ci : cartItems) {
            if (ci.getVariant() == null) {
                continue;
            }

            OrderItem oi = order.getOrderItems().stream()
                    .filter(item -> item.getVariantId().equals(ci.getVariantId()))
                    .findFirst().orElse(null);
            if (oi == null) {
                continue;
            }

            // Claim suat flash sale — nguyen tu qua UPDATE...WHERE (xem
            // PricingService.incrementSoldQuantity / FlashSaleItemRepository.claimQuotaIfAvailable),
            // khong con dua vao findByIdWithLock nua vi pessimistic lock da duoc xac nhan
            // khong chan duoc race o moi truong nay.
            if ("FLASH_SALE".equals(oi.getLoaiGia())) {
                FlashSaleItem item = flashItemMap.get(ci.getVariantId());
                if (item != null && !pricingService.incrementSoldQuantity(item, ci.getSoLuong())) {
                    throw new RuntimeException("Sản phẩm \"" + oi.getTenSanPham() + "\" đã hết suất Flash Sale");
                }
            }

            // Khoa variant, chi KIEM TRA con hang — khong tru o day
            ProductVariant variant = variantRepository.findByIdWithLock(ci.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));
            if (variant.getSoLuongTon() == null || variant.getSoLuongTon() < ci.getSoLuong()) {
                throw new RuntimeException("Sản phẩm \"" + oi.getTenSanPham()
                        + " - " + variant.getTenBienThe() + "\" không đủ hàng trong kho");
            }
        }

        order = orderRepository.save(order);

        if (pointsToRedeem > 0) {
            loyaltyPointsService.redeemPoints(userId, pointsToRedeem, order.getId(),
                    "Đổi điểm cho đơn hàng #" + order.getMaDon());
        }

        orderStatusLogService.ghiLog(order, OrderEventType.CREATE_ORDER, user, null, null, null);

        // COD: dat hang = cam ket mua, xoa gio hang ngay. CHUYEN_KHOAN/SEPAY_QR: don tao
        // truoc khi khach thuc su thanh toan — neu xoa gio hang ngay, khach bo ngang chua
        // chuyen khoan/quet QR se mat trang gio hang oan trong khi chua he thanh toan gi.
        // Chi xoa cac dong nay khoi gio hang luc thanh toan THAT SU duoc xac nhan
        // (xem clearCartItemsForOrder, goi tu confirmPaid / updatePaymentStatus).
        if ("COD".equals(phuongThucTT)) {
            cartItemRepository.deleteAll(cartItems);
        }

        if ("GHN".equals(shippingCarrier)) {
            String ghnCode = ghnShippingService.createOrder(order, address);
            if (ghnCode != null) {
                order.setMaVanDon(ghnCode);
                orderRepository.save(order);
            }
        }

        return order;
    }

    public BigDecimal resolveEligibleAmount(Promotion promo, List<OrderItem> items, Map<Integer, Product> productById) {
        String type = promo.getTargetType() == null ? "" : promo.getTargetType();
        Set<Integer> targetIds = parseIntTargetIds(promo.getTargetIds());
        BigDecimal eligible = BigDecimal.ZERO;
        for (OrderItem item : items) {
            boolean match = switch (type) {
                case "PRODUCT" ->
                    targetIds.contains(item.getProductId());
                case "CATEGORY" -> {
                    Product p = productById.get(item.getProductId());
                    yield p != null && targetIds.contains(p.getDanhMucId());
                }
                default ->
                    true;
            };
            if (!match) {
                continue;
            }
            if ("FLASH_SALE".equals(item.getLoaiGia()) && !Boolean.TRUE.equals(promo.getStackable())) {
                continue;
            }
            eligible = eligible.add(item.getThanhTien());
        }
        return eligible;
    }

    /**
     * Tương đương resolveEligibleAmount nhưng dùng cho giỏ hàng (trước khi tạo Order) —
     * dùng ở trang checkout để xem trước khuyến mãi nào thực sự áp dụng được, tránh
     * tình trạng khuyến mãi tự động chọn hiển thị ở trang xem trước nhưng khi đặt hàng
     * lại bị từ chối do biến thể đang Flash Sale không được cộng dồn khuyến mãi.
     */
    public BigDecimal resolveEligibleAmountForCart(Promotion promo, List<CartItemDTO> items, Map<Integer, Product> productById) {
        String type = promo.getTargetType() == null ? "" : promo.getTargetType();
        Set<Integer> targetIds = parseIntTargetIds(promo.getTargetIds());
        BigDecimal eligible = BigDecimal.ZERO;
        for (CartItemDTO item : items) {
            boolean match = switch (type) {
                case "PRODUCT" ->
                    targetIds.contains(item.getProductId());
                case "CATEGORY" -> {
                    Product p = productById.get(item.getProductId());
                    yield p != null && targetIds.contains(p.getDanhMucId());
                }
                default ->
                    true;
            };
            if (!match) {
                continue;
            }
            if ("FLASH_SALE".equals(item.getNguonGia()) && !Boolean.TRUE.equals(promo.getStackable())) {
                continue;
            }
            eligible = eligible.add(item.getThanhTien());
        }
        return eligible;
    }

    private Set<Integer> parseIntTargetIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private String generateMaDon() {
        return "DH" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String buildFullAddress(Address a) {
        return (a.getDiaChiCuThe() != null ? a.getDiaChiCuThe() + ", " : "")
                + (a.getPhuongXa() != null ? a.getPhuongXa() + ", " : "")
                + (a.getQuanHuyen() != null ? a.getQuanHuyen() + ", " : "")
                + (a.getTinhThanh() != null ? a.getTinhThanh() : "");
    }

    private BigDecimal calculateShipFee(Address address, String shippingCarrier, BigDecimal tienHang) {
        return multiCarrierShippingService.calculateFeeForCarrier(shippingCarrier, address, tienHang);
    }

    public void validatePromotion(Promotion promo, BigDecimal tienHang) {
        if (!promo.getIsActive()) {
            throw new RuntimeException("Mã giảm giá không hoạt động");
        }
        if (promo.getDenNgay() != null && promo.getDenNgay().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }
        if (promo.getTuNgay() != null && promo.getTuNgay().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá chưa đến hạn sử dụng");
        }
        if (promo.getSoLanDung() != null && promo.getDaDung() >= promo.getSoLanDung()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }
        if ("PHAN_TRAM".equals(promo.getLoaiGiam()) && promo.getGiaTriGiam() != null
                && promo.getGiaTriGiam().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("Mã giảm giá không hợp lệ (phần trăm vượt quá 100%)");
        }
        if (promo.getDonHangToiThieu() != null && tienHang.compareTo(promo.getDonHangToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu " + PriceUtils.format(promo.getDonHangToiThieu()) + " để áp dụng mã");
        }
        if (promo.getBudget() != null && promo.getUsedBudget() != null
                && promo.getUsedBudget().compareTo(promo.getBudget()) >= 0) {
            throw new RuntimeException("Mã giảm giá đã hết ngân sách");
        }
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal tienHang) {
        return calculateDiscount(promo, tienHang, BigDecimal.ZERO);
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal tienHang, BigDecimal phiVanChuyen) {
        if ("FREESHIP".equals(promo.getLoaiGiam())) {
            return phiVanChuyen != null ? phiVanChuyen : BigDecimal.ZERO;
        }
        BigDecimal discount;
        if ("PHAN_TRAM".equals(promo.getLoaiGiam())) {
            BigDecimal pct = promo.getGiaTriGiam() != null ? promo.getGiaTriGiam() : BigDecimal.ZERO;
            if (pct.compareTo(new BigDecimal("100")) > 0) {
                pct = new BigDecimal("100");
            }
            discount = tienHang.multiply(pct).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (promo.getGiamToiDa() != null && discount.compareTo(promo.getGiamToiDa()) > 0) {
                discount = promo.getGiamToiDa();
            }
        } else {
            discount = promo.getGiaTriGiam();
            if (discount.compareTo(tienHang) > 0) {
                discount = tienHang;
            }
        }
        return discount;
    }

    public Page<Order> getOrdersByUserId(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayDat"));
        return orderRepository.findByUserId(userId, pageable);
    }

    public Page<Order> getOrdersByUserIdAndStatus(Integer userId, String trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayDat"));
        return orderRepository.findByUserIdAndTrangThaiDon(userId, trangThai, pageable);
    }

    public Order getOrderById(Integer id) {
        return orderRepository.findByIdWithUserAndItems(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    public Order getOrderByUserAndId(Integer userId, Integer orderId) {
        Order order = getOrderById(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền xem đơn hàng này");
        }
        return order;
    }

    public Order getOrderByMaDon(String maDon) {
        return orderRepository.findByMaDonWithUserAndItems(maDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    public List<TimelineEvent> getOrderTimeline(String maDon) {
        Order order = getOrderByMaDon(maDon);
        List<OrderStatusLog> logs = orderStatusLogService.getLogsByOrder(order.getId());
        List<TimelineEvent> events = new ArrayList<>();
        for (int i = 0; i < logs.size(); i++) {
            OrderStatusLog log = logs.get(i);
            String desc = switch (log.getLoaiSuKien()) {
                case CREATE_ORDER -> "Đã đặt hàng";
                case PAYMENT_CONFIRMED -> "Đã thanh toán";
                case ASSIGN_ADMIN -> "Đã phân công xử lý";
                case STATUS_CHANGE -> {
                    String s = log.getTrangThaiMoi() != null ? log.getTrangThaiMoi() : "";
                    yield switch (s) {
                        case "DA_XAC_NHAN" -> "Đã xác nhận đơn hàng";
                        case "DANG_GIAO" -> "Đang giao hàng";
                        case "DA_GIAO" -> "Đã giao hàng";
                        case "DA_HOAN_THANH" -> "Hoàn thành";
                        default -> "Cập nhật: " + s;
                    };
                }
                case CANCEL_ORDER -> "Đã hủy đơn hàng";
            };
            boolean isLast = (i == logs.size() - 1);
            events.add(new TimelineEvent(
                    desc,
                    log.getGhiChu() != null ? log.getGhiChu() : "",
                    log.getThoiGian(),
                    log.getLoaiSuKien().name(),
                    true,
                    isLast
            ));
        }
        return events;
    }

    /**
     * UPDATE nguyen tu (giong confirmPaid) — webhook SePay hay retry, 2 request IPN gan nhu
     * dong thoi cho cung 1 don phai chi co DUY NHAT 1 request "thang" duoc coi la lan dau
     * chuyen CHUA_THANH_TOAN -> DA_THANH_TOAN, khong thi ca hai deu chay
     * clearCartItemsForOrder/autoAssignAndNotify/gui email trung (da xac nhan bang test
     * thuc te truoc khi doi sang UPDATE nguyen tu).
     *
     * Tra ve true CHI KHI chinh lan goi nay la lan dau chuyen trang thai — caller (vd
     * sepayIPN) phai dua vao gia tri tra ve nay de quyet dinh co ghi log/gui email
     * "thanh toan thanh cong" hay khong, KHONG duoc tu doc lai trang thai don rieng.
     */
    @Transactional
    public boolean updatePaymentStatus(Integer orderId, String trangThaiTT) {
        if ("DA_THANH_TOAN".equals(trangThaiTT)) {
            int updated = orderRepository.markPaidIfUnpaid(orderId);
            if (updated == 0) {
                return false;
            }
            Order order = orderRepository.findByIdWithUserAndItems(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            clearCartItemsForOrder(order);
            autoAssignAndNotify(order);
            return true;
        }
        Order order = getOrderById(orderId);
        order.setTrangThaiTT(trangThaiTT);
        orderRepository.save(order);
        return false;
    }

    /**
     * UPDATE nguyen tu (xem OrderRepository.markCompletedIfDelivered) — chong khach
     * double-click "Đã nhận được hàng" hoac dung do voi admin doi trang thai gan nhu
     * cung luc cong diem/gui email cam on 2 lan.
     */
    @Transactional
    public void markOrderReceived(Integer userId, Integer orderId) {
        Order order = getOrderByUserAndId(userId, orderId);
        int updated = orderRepository.markCompletedIfDelivered(orderId);
        if (updated == 0) {
            throw new RuntimeException("Chỉ có thể xác nhận đã nhận khi đơn hàng ở trạng thái 'Đã giao'");
        }
        order.setTrangThaiDon("DA_HOAN_THANH");
        order.setTrangThaiTT("DA_THANH_TOAN");
        orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, null,
                "DA_GIAO", "DA_HOAN_THANH",
                "Khách hàng xác nhận đã nhận được hàng");
        if (order.getUser() != null) {
            loyaltyPointsService.earnPoints(userId, orderId, order.getTongThanhToan());
        }
        notifyOrderCompleted(order);
    }

    @Transactional
    public void cancelOrder(Integer userId, Integer orderId, String lyDo) {
        Order order = getOrderByUserAndId(userId, orderId);
        if (!"CHO_XAC_NHAN".equals(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }
        // Khong can hoan lai ton kho: o trang thai CHO_XAC_NHAN, ton kho THUC TE chua
        // bi tru (chi tru khi don chuyen sang "Dang giao" — xem AdminOrderService.adjustStock).
        restoreFlashSaleQuota(orderId);
        restoreVoucher(order);
        loyaltyPointsService.refundRedeemedPointsForOrder(userId, orderId);
        orderAssignmentRepository.findByOrderId(orderId).ifPresent(orderAssignmentRepository::delete);
        order.setTrangThaiDon("DA_HUY");
        orderRepository.save(order);

        orderStatusLogService.ghiLog(order, OrderEventType.CANCEL_ORDER, null,
                "CHO_XAC_NHAN", "DA_HUY",
                lyDo != null && !lyDo.isBlank() ? lyDo : "Khách hàng hủy đơn (không có lý do)");
    }

    @Transactional
    public void restoreVoucherForOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            restoreVoucher(order);
        }
    }

    private void restoreVoucher(Order order) {
        if (order.getPromotion() == null) {
            return;
        }
        Promotion promo = promotionRepository.findByIdWithLock(order.getPromotion().getId()).orElse(null);
        if (promo == null) {
            return;
        }
        int daDung = promo.getDaDung() != null ? promo.getDaDung() : 0;
        promo.setDaDung(Math.max(0, daDung - 1));
        BigDecimal tienGiam = order.getTienGiam() != null ? order.getTienGiam() : BigDecimal.ZERO;
        BigDecimal usedBudget = promo.getUsedBudget() != null ? promo.getUsedBudget() : BigDecimal.ZERO;
        promo.setUsedBudget(usedBudget.subtract(tienGiam).max(BigDecimal.ZERO));
        promotionRepository.save(promo);

        if (order.getUser() == null) {
            return;
        }
        UserVoucher uv = userVoucherRepository.findByUserIdAndPromotionId(order.getUser().getId(), promo.getId())
                .orElse(null);
        if (uv == null) {
            return;
        }
        Integer remaining = uv.getRemainingUses();
        if (remaining != null) {
            uv.setRemainingUses(remaining + 1);
        } else {
            uv.setRemainingUses(1);
        }
        if (uv.getStatus() == VoucherStatus.USED) {
            uv.setStatus(VoucherStatus.AVAILABLE);
            uv.setUsedAt(null);
        }
        BigDecimal totalSaved = uv.getTotalSaved() != null ? uv.getTotalSaved() : BigDecimal.ZERO;
        uv.setTotalSaved(totalSaved.subtract(tienGiam).max(BigDecimal.ZERO));
        userVoucherRepository.save(uv);
    }

    private void restoreFlashSaleQuota(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            if (!"FLASH_SALE".equals(item.getLoaiGia()) || item.getVariantId() == null) {
                continue;
            }
            flashSaleItemRepository.findByVariantId(item.getVariantId())
                    .stream()
                    .findFirst()
                    .ifPresent(found -> pricingService.decrementSoldQuantity(found, item.getSoLuong()));
        }
    }

    public Map<String, Object> validateCouponForApi(String maCode, BigDecimal tienHang, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);

        if (maCode == null || maCode.isBlank()) {
            result.put("message", "Vui lòng nhập mã giảm giá");
            return result;
        }

        Promotion promo = promotionRepository.findByMaCodeIgnoreCaseAndIsActiveTrue(maCode.trim())
                .orElse(null);
        if (promo == null) {
            result.put("message", "Mã giảm giá không tồn tại hoặc đã bị vô hiệu hóa");
            return result;
        }

        result.put("loaiGiam", promo.getLoaiGiam());
        result.put("giaTriGiam", promo.getGiaTriGiam());
        result.put("giamToiDa", promo.getGiamToiDa());
        result.put("donHangToiThieu", promo.getDonHangToiThieu());
        result.put("tenChuongTrinh", promo.getTenChuongTrinh());
        // Không bắt buộc phải lưu voucher vào ví mới được dùng (đã bỏ ràng buộc này theo
        // yêu cầu trước) — nhưng vẫn báo cho client biết mã này KHÔNG nằm trong ví của
        // khách, để client tự quyết định hiển thị 1 cảnh báo minh bạch thay vì áp dụng
        // âm thầm như trước (quan trọng với voucher phải mua bằng điểm tích lũy).
        boolean ownedInWallet = userId != null
                && userVoucherRepository.findByUserIdAndPromotionId(userId, promo.getId()).isPresent();
        result.put("ownedInWallet", ownedInWallet);

        try {
            validatePromotion(promo, tienHang);
            BigDecimal discount = calculateDiscount(promo, tienHang);
            result.put("valid", true);
            result.put("discount", discount);
            result.put("message", "Áp dụng mã thành công! Giảm " + formatDiscount(promo, discount));
        } catch (RuntimeException e) {
            result.put("message", e.getMessage());
        }

        return result;
    }

    private String formatDiscount(Promotion promo, BigDecimal discount) {
        if ("PHAN_TRAM".equals(promo.getLoaiGiam())) {
            String pct = promo.getGiaTriGiam().stripTrailingZeros().toPlainString();
            if (promo.getGiamToiDa() != null) {
                return pct + "% (tối đa " + formatVND(promo.getGiamToiDa()) + ")";
            }
            return pct + "%";
        }
        return formatVND(discount);
    }

    private String formatVND(BigDecimal amount) {
        return PriceUtils.format(amount);
    }

    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setMaDon(order.getMaDon());
        try {
            dto.setUserId(order.getUser().getId());
            dto.setUserEmail(order.getUser().getEmail());
        } catch (ObjectNotFoundException e) {
            dto.setUserId(null);
            dto.setUserEmail("Người dùng đã xoá");
        }
        dto.setTenNguoiNhan(order.getSnapTenNguoiNhan());
        dto.setSoDienThoai(order.getSnapSoDienThoai());
        dto.setDiaChi(order.getSnapDiaChi());
        dto.setTienHang(order.getTienHang());
        dto.setPhiVanChuyen(order.getPhiVanChuyen());
        dto.setTienGiam(order.getTienGiam());
        dto.setTongThanhToan(order.getTongThanhToan());
        dto.setPhuongThucTT(order.getPhuongThucTT());
        dto.setPhuongThucGiaoHang(order.getPhuongThucGiaoHang());
        dto.setTrangThaiTT(order.getTrangThaiTT());
        dto.setTrangThaiDon(order.getTrangThaiDon());
        dto.setGhiChu(order.getGhiChu());
        dto.setMaVanDon(order.getMaVanDon());
        dto.setFraudWarning(order.getFraudWarning());
        dto.setShippingCarrier(order.getShippingCarrier());
        dto.setNgayDat(order.getNgayDat());
        if (order.getPromotion() != null) {
            dto.setPromotionId(order.getPromotion().getId());
        }
        return dto;
    }

    public OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariantId());
        dto.setTenSanPham(item.getTenSanPham());
        dto.setTenBienThe(item.getTenBienThe());
        dto.setHinhAnhSP(item.getHinhAnhSP());
        dto.setDonGia(item.getDonGia());
        dto.setSoLuong(item.getSoLuong());
        dto.setThanhTien(item.getThanhTien());
        dto.setLoaiGia(item.getLoaiGia());
        return dto;
    }

    public List<OrderItemDTO> getOrderItemsByOrder(Order order) {
        return orderItemRepository.findByOrderId(order.getId()).stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }
}
