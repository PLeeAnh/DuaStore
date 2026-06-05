package com.duastore.service.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.*;
import com.duastore.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartService cartService, AddressRepository addressRepository,
                        PromotionRepository promotionRepository, UserRepository userRepository,
                        CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartService = cartService;
        this.addressRepository = addressRepository;
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    private static final String[] PHUONG_THUC_TT = {"COD", "CHUYEN_KHOAN", "VNPAY"};
    private static final String[] PHUONG_THUC_GH = {"SHIP", "NHANH"};
    private static final BigDecimal PHI_SHIP = new BigDecimal("30000");
    private static final BigDecimal PHI_SHIP_NHANH = new BigDecimal("50000");

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
        order.setPhiVanChuyen(calculateShipFee(phuongThucGiaoHang));

        BigDecimal tienHang = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            Product product = ci.getProduct();
            ProductVariant variant = ci.getVariant();
            BigDecimal donGia = variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();
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
            order.getOrderItems().add(item);
        }
        order.setTienHang(tienHang);

        if (maCode != null && !maCode.isBlank()) {
            Promotion promo = promotionRepository.findByMaCodeAndIsActiveTrue(maCode.toUpperCase().trim())
                    .orElse(null);
            if (promo != null) {
                validatePromotion(promo, tienHang);
                BigDecimal tienGiam = calculateDiscount(promo, tienHang);
                order.setTienGiam(tienGiam);
                order.setPromotion(promo);
                promo.setDaDung(promo.getDaDung() + 1);
            }
        }

        BigDecimal tong = order.getTienHang().add(order.getPhiVanChuyen()).subtract(order.getTienGiam());
        if (tong.compareTo(BigDecimal.ZERO) < 0) tong = BigDecimal.ZERO;
        order.setTongThanhToan(tong);

        order = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return order;
    }

    private String generateMaDon() {
        String prefix = "DH";
        String ts = String.valueOf(System.currentTimeMillis());
        String rand = String.format("%03d", new Random().nextInt(1000));
        return prefix + ts.substring(ts.length() - 8) + rand;
    }

    private String buildFullAddress(Address a) {
        return (a.getDiaChiCuThe() != null ? a.getDiaChiCuThe() + ", " : "")
                + (a.getPhuongXa() != null ? a.getPhuongXa() + ", " : "")
                + (a.getQuanHuyen() != null ? a.getQuanHuyen() + ", " : "")
                + (a.getTinhThanh() != null ? a.getTinhThanh() : "");
    }

    private BigDecimal calculateShipFee(String phuongThucGH) {
        if ("NHANH".equalsIgnoreCase(phuongThucGH)) return PHI_SHIP_NHANH;
        return PHI_SHIP;
    }

    private void validatePromotion(Promotion promo, BigDecimal tienHang) {
        if (!promo.getIsActive()) throw new RuntimeException("Mã giảm giá không hoạt động");
        if (promo.getDenNgay() != null && promo.getDenNgay().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        if (promo.getTuNgay() != null && promo.getTuNgay().isAfter(LocalDateTime.now()))
            throw new RuntimeException("Mã giảm giá chưa đến hạn sử dụng");
        if (promo.getSoLanDung() != null && promo.getDaDung() >= promo.getSoLanDung())
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        if (tienHang.compareTo(promo.getDonHangToiThieu()) < 0)
            throw new RuntimeException("Đơn hàng tối thiểu " + promo.getDonHangToiThieu() + "đ để áp dụng mã");
    }

    private BigDecimal calculateDiscount(Promotion promo, BigDecimal tienHang) {
        BigDecimal discount;
        if ("PHAN_TRAM".equals(promo.getLoaiGiam())) {
            discount = tienHang.multiply(promo.getGiaTriGiam()).divide(new BigDecimal("100"));
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
    public void cancelOrder(Integer userId, Integer orderId) {
        Order order = getOrderByUserAndId(userId, orderId);
        if (!"CHO_XAC_NHAN".equals(order.getTrangThaiDon())) {
            throw new RuntimeException("Chỉ có thể hủy đơn hàng đang chờ xác nhận");
        }
        order.setTrangThaiDon("DA_HUY");
        orderRepository.save(order);
    }

    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setMaDon(order.getMaDon());
        dto.setUserId(order.getUser().getId());
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
        return dto;
    }

    public List<OrderItemDTO> getOrderItemsByOrder(Order order) {
        return orderItemRepository.findByOrderId(order.getId()).stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
    }
}
