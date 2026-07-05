package com.duastore.service.client;

import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.*;
import com.duastore.repository.*;
import com.duastore.service.GHNShippingService;
import com.duastore.service.PricingService;
import com.duastore.service.ShippingFeeService;
import com.duastore.service.VNPAYService;
import com.duastore.service.admin.OrderStatusLogService;
import com.duastore.util.PriceUtils;
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
    private final ShippingFeeService shippingFeeService;
    private final ProductVariantRepository variantRepository;
    private final OrderStatusLogService orderStatusLogService;
    private final UserVoucherRepository userVoucherRepository;
    private final GHNShippingService ghnShippingService;
    private final VNPAYService vnpayService;
    private final PricingService pricingService;
    private final FlashSaleRepository flashSaleRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartService cartService, AddressRepository addressRepository,
                        PromotionRepository promotionRepository, UserRepository userRepository,
                        CartItemRepository cartItemRepository,
                        OrderAssignmentRepository orderAssignmentRepository,
                        ShippingFeeService shippingFeeService,
                        ProductVariantRepository variantRepository,
                        OrderStatusLogService orderStatusLogService,
                        UserVoucherRepository userVoucherRepository,
                        GHNShippingService ghnShippingService,
                        VNPAYService vnpayService,
                        PricingService pricingService,
                        FlashSaleRepository flashSaleRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
        this.shippingFeeService = shippingFeeService;
        this.variantRepository = variantRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.userVoucherRepository = userVoucherRepository;
        this.ghnShippingService = ghnShippingService;
        this.vnpayService = vnpayService;
        this.pricingService = pricingService;
        this.flashSaleRepository = flashSaleRepository;
    }



    @Transactional
    public Order processCheckout(Integer userId, Integer addressId, String phuongThucTT,
                                  String phuongThucGiaoHang, String maCode, String ghiChu) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Địa chỉ không hợp lệ");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByNgayThemDesc(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // Load flash sale map once for all items
        List<Integer> productIds = cartItems.stream()
                .map(CartItem::getProductId).distinct().collect(Collectors.toList());
        Map<Integer, FlashSale> flashSaleMap = pricingService.loadActiveFlashSaleMap(productIds);

        Order order = new Order();
        order.setMaDon(generateMaDon());
        order.setUser(user);
        order.setAddressId(addressId);
        order.setSnapTenNguoiNhan(address.getTenNguoiNhan());
        order.setSnapSoDienThoai(address.getSoDienThoai());
        order.setSnapDiaChi(buildFullAddress(address));
        order.setPhuongThucTT(phuongThucTT);
        order.setPhuongThucGiaoHang(phuongThucGiaoHang);
        order.setGhiChu(ghiChu);
        order.setPhiVanChuyen(calculateShipFee(address, phuongThucGiaoHang));

        BigDecimal tienHang = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            ProductVariant variant = ci.getVariant();
            PricingService.PriceResult priced = pricingService.resolvePrice(variant, flashSaleMap.get(product.getId()));
            BigDecimal donGia = priced.finalPrice();
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
            item.setLoaiGia(priced.source().name());
            order.getOrderItems().add(item);
        }
        order.setTienHang(tienHang);

        if (maCode != null && !maCode.isBlank()) {
            Promotion promo = promotionRepository.findByMaCodeIgnoreCaseAndIsActiveTrue(maCode.trim())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá \"" + maCode + "\" không tồn tại hoặc đã bị vô hiệu hóa"));
            Promotion lockedPromo = promotionRepository.findByIdWithLock(promo.getId())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

            Map<Integer, Product> productById = cartItems.stream()
                    .map(CartItem::getProduct)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
            BigDecimal eligibleAmount = resolveEligibleAmount(lockedPromo, order.getOrderItems(), productById);
            validatePromotion(lockedPromo, eligibleAmount);
            BigDecimal tienGiam = calculateDiscount(lockedPromo, eligibleAmount);
            order.setTienGiam(tienGiam);
            order.setPromotion(lockedPromo);
            lockedPromo.setDaDung(lockedPromo.getDaDung() + 1);
            BigDecimal usedBudget = lockedPromo.getUsedBudget() != null ? lockedPromo.getUsedBudget() : BigDecimal.ZERO;
            lockedPromo.setUsedBudget(usedBudget.add(tienGiam));
            promotionRepository.save(lockedPromo);
            userVoucherRepository.findByUserIdAndPromotionId(userId, lockedPromo.getId()).ifPresent(uv -> {
                uv.setStatus(VoucherStatus.USED);
                uv.setUsedAt(LocalDateTime.now());
                uv.setTotalSaved(uv.getTotalSaved().add(tienGiam));
                userVoucherRepository.save(uv);
            });
        }

        if (order.getTienGiam() == null) order.setTienGiam(BigDecimal.ZERO);

        BigDecimal tong = order.getTienHang().add(order.getPhiVanChuyen()).subtract(order.getTienGiam());
        if (tong.compareTo(BigDecimal.ZERO) < 0) tong = BigDecimal.ZERO;
        order.setTongThanhToan(tong);

        order = orderRepository.save(order);

        orderStatusLogService.ghiLog(order, OrderEventType.CREATE_ORDER, user, null, null, null);

        // Lock flash sale + decrement stock
        for (CartItem ci : cartItems) {
            if (ci.getVariant() == null) continue;

            OrderItem oi = order.getOrderItems().stream()
                    .filter(item -> item.getVariantId().equals(ci.getVariantId()))
                    .findFirst().orElse(null);
            if (oi == null) continue;

            // Lock and increment flash sale sold count first
            if ("FLASH_SALE".equals(oi.getLoaiGia())) {
                FlashSale fs = flashSaleMap.get(ci.getProductId());
                if (fs != null) {
                    FlashSale lockedFs = flashSaleRepository.findByIdWithLock(fs.getId())
                            .orElseThrow(() -> new RuntimeException("Flash sale không tồn tại"));
                    if (!pricingService.incrementSoldQuantity(lockedFs, ci.getSoLuong())) {
                        throw new RuntimeException("Sản phẩm \"" + oi.getTenSanPham() + "\" đã hết suất Flash Sale");
                    }
                    flashSaleRepository.save(lockedFs);
                }
            }

            // Lock variant and decrement stock
            ProductVariant variant = variantRepository.findByIdWithLock(ci.getVariant().getId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho"));
            if (variant.getSoLuongTon() < ci.getSoLuong()) {
                throw new RuntimeException("Sản phẩm \"" + oi.getTenSanPham()
                        + " - " + variant.getTenBienThe() + "\" không đủ hàng trong kho");
            }
            variant.setSoLuongTon(variant.getSoLuongTon() - ci.getSoLuong());
            variantRepository.save(variant);
        }

        cartItemRepository.deleteAll(cartItems);

        String ghnCode = ghnShippingService.createOrder(order, address);
        if (ghnCode != null) {
            order.setMaVanDon(ghnCode);
            orderRepository.save(order);
        }

        return order;
    }

    public BigDecimal resolveEligibleAmount(Promotion promo, List<OrderItem> items, Map<Integer, Product> productById) {
        String type = promo.getTargetType() == null ? "" : promo.getTargetType();
        Set<Integer> targetIds = parseIntTargetIds(promo.getTargetIds());
        BigDecimal eligible = BigDecimal.ZERO;
        for (OrderItem item : items) {
            boolean match = switch (type) {
                case "PRODUCT" -> targetIds.contains(item.getProductId());
                case "CATEGORY" -> {
                    Product p = productById.get(item.getProductId());
                    yield p != null && targetIds.contains(p.getDanhMucId());
                }
                default -> true;
            };
            if (!match) continue;
            if ("FLASH_SALE".equals(item.getLoaiGia()) && !Boolean.TRUE.equals(promo.getStackable())) continue;
            eligible = eligible.add(item.getThanhTien());
        }
        return eligible;
    }

    private Set<Integer> parseIntTargetIds(String raw) {
        if (raw == null || raw.isBlank()) return Set.of();
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

    private BigDecimal calculateShipFee(Address address, String phuongThucGH) {
        return shippingFeeService.calculateFee(address, phuongThucGH);
    }

    public void validatePromotion(Promotion promo, BigDecimal tienHang) {
        if (!promo.getIsActive()) throw new RuntimeException("Mã giảm giá không hoạt động");
        if (promo.getDenNgay() != null && promo.getDenNgay().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        if (promo.getTuNgay() != null && promo.getTuNgay().isAfter(LocalDateTime.now()))
            throw new RuntimeException("Mã giảm giá chưa đến hạn sử dụng");
        if (promo.getSoLanDung() != null && promo.getDaDung() >= promo.getSoLanDung())
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        if (tienHang.compareTo(promo.getDonHangToiThieu()) < 0)
            throw new RuntimeException("Đơn hàng tối thiểu " + PriceUtils.format(promo.getDonHangToiThieu()) + " để áp dụng mã");
        if (promo.getBudget() != null && promo.getUsedBudget().compareTo(promo.getBudget()) >= 0)
            throw new RuntimeException("Mã giảm giá đã hết ngân sách");
    }

    public BigDecimal calculateDiscount(Promotion promo, BigDecimal tienHang) {
        BigDecimal discount;
        if ("PHAN_TRAM".equals(promo.getLoaiGiam())) {
            discount = tienHang.multiply(promo.getGiaTriGiam()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (promo.getGiamToiDa() != null && discount.compareTo(promo.getGiamToiDa()) > 0) {
                discount = promo.getGiamToiDa();
            }
        } else {
            discount = promo.getGiaTriGiam();
            if (discount.compareTo(tienHang) > 0) discount = tienHang;
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

    @Transactional
    public void updatePaymentStatus(Integer orderId, String trangThaiTT) {
        Order order = getOrderById(orderId);
        order.setTrangThaiTT(trangThaiTT);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Integer userId, Integer orderId, String lyDo) {
        Order order = getOrderByUserAndId(userId, orderId);
        if (!"CHO_XAC_NHAN".equals(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }
        restoreStock(orderId);
        restoreFlashSaleQuota(orderId);
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
            if (!"FLASH_SALE".equals(item.getLoaiGia())) continue;
            flashSaleRepository.findByProductIdInAndIsActiveTrue(List.of(item.getProductId()))
                    .stream().findFirst()
                    .ifPresent(fs -> {
                        FlashSale lockedFs = flashSaleRepository.findByIdWithLock(fs.getId()).orElse(null);
                        if (lockedFs != null) {
                            pricingService.decrementSoldQuantity(lockedFs, item.getSoLuong());
                            flashSaleRepository.save(lockedFs);
                        }
                    });
        }
    }

    private void restoreStock(Integer orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            if (item.getVariantId() == null) continue;
            ProductVariant variant = variantRepository.findByIdWithLock(item.getVariantId()).orElse(null);
            if (variant == null) continue;
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
        dto.setUserId(order.getUser().getId());
        dto.setUserEmail(order.getUser().getEmail());
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
