package com.duastore.service.client;

import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.dto.TimelineEvent;
import com.duastore.model.*;
import com.duastore.repository.*;
import com.duastore.service.GHNShippingService;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.MultiCarrierShippingService;
import com.duastore.service.PricingService;
import com.duastore.service.VNPAYService;
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
import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional(readOnly = true)
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
    private final VNPAYService vnpayService;
    private final PricingService pricingService;
    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final LoyaltyPointsService loyaltyPointsService;
    private final MultiCarrierShippingService multiCarrierShippingService;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            CartService cartService, AddressRepository addressRepository,
            PromotionRepository promotionRepository, UserRepository userRepository,
            CartItemRepository cartItemRepository,
            OrderAssignmentRepository orderAssignmentRepository,
            ProductVariantRepository variantRepository,
            OrderStatusLogService orderStatusLogService,
            UserVoucherRepository userVoucherRepository,
            GHNShippingService ghnShippingService,
            VNPAYService vnpayService,
            PricingService pricingService,
            FlashSaleRepository flashSaleRepository,
            FlashSaleItemRepository flashSaleItemRepository,
            LoyaltyPointsService loyaltyPointsService,
            MultiCarrierShippingService multiCarrierShippingService) {
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
        this.vnpayService = vnpayService;
        this.pricingService = pricingService;
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.loyaltyPointsService = loyaltyPointsService;
        this.multiCarrierShippingService = multiCarrierShippingService;
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
            BigDecimal donGia = ci.getGiaLucThem();
            String loaiGia = "THUONG";
            // Always re-resolve current price so flash-sale quota is tracked correctly
            PricingService.PriceResult priced = pricingService.resolvePrice(variant, flashItemMap.get(variant.getId()));
            donGia = priced.finalPrice();
            loaiGia = priced.source().name();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(ci.getSoLuong()));
            tienHang = tienHang.add(thanhTien);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setVariantId(variant.getId());
            item.setTenSanPham(product.getTenSanPham());
            item.setTenBienThe(variant.getTenBienThe());
            item.setHinhAnhSP(variant.getHinhAnh() != null ? variant.getHinhAnh() : product.getHinhAnhChinh());
            item.setDonGia(donGia);
            item.setSoLuong(ci.getSoLuong());
            item.setThanhTien(thanhTien);
            item.setLoaiGia(loaiGia);
            order.getOrderItems().add(item);
        }
        order.setTienHang(tienHang);
        order.setPhiVanChuyen(calculateShipFee(address, shippingCarrier, tienHang));

        if (maCode != null && !maCode.isBlank()) {
            Promotion promo = promotionRepository.findByMaCodeIgnoreCaseAndIsActiveTrue(maCode.trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá \"" + maCode + "\" không tồn tại hoặc đã bị vô hiệu hóa"));
            Promotion lockedPromo = promotionRepository.findByIdWithLock(promo.getId())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

            // Voucher trong ví: chi duoc dung neu con hieu luc va con luot
            UserVoucher userVoucher = userVoucherRepository.findByUserIdAndPromotionId(userId, lockedPromo.getId())
                    .orElse(null);
            if (userVoucher != null) {
                if (userVoucher.getStatus() != VoucherStatus.USED && userVoucher.getStatus() != VoucherStatus.AVAILABLE) {
                    throw new RuntimeException("Voucher này không còn sử dụng được");
                }
                if (userVoucher.getExpiredAt() != null && userVoucher.getExpiredAt().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Voucher đã hết hạn");
                }
                if (userVoucher.getRemainingUses() != null && userVoucher.getRemainingUses() <= 0) {
                    throw new RuntimeException("Voucher đã hết lượt sử dụng");
                }
            }

            Map<Integer, Product> productById = cartItems.stream()
                    .map(CartItem::getProduct)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
            BigDecimal eligibleAmount = resolveEligibleAmount(lockedPromo, order.getOrderItems(), productById);
            validatePromotion(lockedPromo, eligibleAmount);
            BigDecimal tienGiam = calculateDiscount(lockedPromo, eligibleAmount,
                    order.getPhiVanChuyen() != null ? order.getPhiVanChuyen() : BigDecimal.ZERO);
            order.setTienGiam(tienGiam);
            order.setPromotion(lockedPromo);
            lockedPromo.setDaDung(lockedPromo.getDaDung() + 1);
            BigDecimal usedBudget = lockedPromo.getUsedBudget() != null ? lockedPromo.getUsedBudget() : BigDecimal.ZERO;
            lockedPromo.setUsedBudget(usedBudget.add(tienGiam));
            promotionRepository.save(lockedPromo);

            if (userVoucher != null) {
                Integer remaining = userVoucher.getRemainingUses();
                if (remaining != null && remaining > 1) {
                    userVoucher.setRemainingUses(remaining - 1);
                    userVoucher.setTotalSaved(userVoucher.getTotalSaved().add(tienGiam));
                } else {
                    userVoucher.setRemainingUses(0);
                    userVoucher.setStatus(VoucherStatus.USED);
                    userVoucher.setUsedAt(LocalDateTime.now());
                    userVoucher.setTotalSaved(userVoucher.getTotalSaved().add(tienGiam));
                }
                userVoucherRepository.save(userVoucher);
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

        // Lock flash sale + decrement stock FIRST (before saving order)
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

            // Lock and increment flash sale sold count first
            if ("FLASH_SALE".equals(oi.getLoaiGia())) {
                FlashSaleItem item = flashItemMap.get(ci.getVariantId());
                if (item != null) {
                    FlashSaleItem lockedItem = flashSaleItemRepository.findByIdWithLock(item.getId())
                            .orElseThrow(() -> new RuntimeException("Flash sale không tồn tại"));
                    if (!pricingService.incrementSoldQuantity(lockedItem, ci.getSoLuong())) {
                        throw new RuntimeException("Sản phẩm \"" + oi.getTenSanPham() + "\" đã hết suất Flash Sale");
                    }
                    flashSaleItemRepository.save(lockedItem);
                }
            }

            // Lock variant and decrement stock atomically
            ProductVariant variant = variantRepository.findByIdWithLock(ci.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));
            int affected = variantRepository.decrementStock(variant.getId(), ci.getSoLuong());
            if (affected == 0) {
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

        cartItemRepository.deleteAll(cartItems);

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
        if (tienHang.compareTo(promo.getDonHangToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng tối thiểu " + PriceUtils.format(promo.getDonHangToiThieu()) + " để áp dụng mã");
        }
        if (promo.getBudget() != null && promo.getUsedBudget().compareTo(promo.getBudget()) >= 0) {
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
            discount = tienHang.multiply(promo.getGiaTriGiam()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
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
        return orderRepository.findById(id)
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
        return orderRepository.findByMaDon(maDon)
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
                case REFUND_ORDER -> "Đã hoàn tiền";
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

    @Transactional
    public void updatePaymentStatus(Integer orderId, String trangThaiTT) {
        Order order = getOrderById(orderId);
        order.setTrangThaiTT(trangThaiTT);
        orderRepository.save(order);
    }

    @Transactional
    public void markOrderReceived(Integer userId, Integer orderId) {
        Order order = getOrderByUserAndId(userId, orderId);
        if (!"DA_GIAO".equals(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể xác nhận đã nhận khi đơn hàng ở trạng thái 'Đã giao'");
        }
        order.setTrangThaiDon("DA_HOAN_THANH");
        order.setTrangThaiTT("DA_THANH_TOAN");
        orderRepository.save(order);
        orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, null,
                "DA_GIAO", "DA_HOAN_THANH",
                "Khách hàng xác nhận đã nhận được hàng");
    }

    @Transactional
    public void cancelOrder(Integer userId, Integer orderId, String lyDo) {
        Order order = getOrderByUserAndId(userId, orderId);
        if (!"CHO_XAC_NHAN".equals(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }
        restoreStock(orderId);
        restoreFlashSaleQuota(orderId);
        loyaltyPointsService.refundRedeemedPointsForOrder(userId, orderId);
        orderAssignmentRepository.findByOrderId(orderId).ifPresent(orderAssignmentRepository::delete);
        order.setTrangThaiDon("DA_HUY");
        orderRepository.save(order);

        orderStatusLogService.ghiLog(order, OrderEventType.CANCEL_ORDER, null,
                "CHO_XAC_NHAN", "DA_HUY",
                lyDo != null && !lyDo.isBlank() ? lyDo : "Khách hàng hủy đơn (không có lý do)");
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
                    .flatMap(found -> flashSaleItemRepository.findByIdWithLock(found.getId()))
                    .ifPresent(lockedItem -> {
                        pricingService.decrementSoldQuantity(lockedItem, item.getSoLuong());
                        flashSaleItemRepository.save(lockedItem);
                    });
        }
    }

    private void restoreStock(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getVariantId() == null) {
                continue;
            }
            ProductVariant variant = variantRepository.findByIdWithLock(item.getVariantId()).orElse(null);
            if (variant == null) {
                continue;
            }
            variant.setSoLuongTon(variant.getSoLuongTon() + item.getSoLuong());
            variantRepository.save(variant);
        }
    }

    public Map<String, Object> validateCouponForApi(String maCode, BigDecimal tienHang) {
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

    public String createVNPAYPaymentUrl(Integer orderId, HttpServletRequest req) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        return vnpayService.createPaymentUrl(
                "DUASTORE" + order.getId(),
                order.getTongThanhToan().longValue(),
                "Thanh toan don hang " + order.getMaDon(),
                req
        );
    }
}
