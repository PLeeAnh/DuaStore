package com.duastore.service.client;

import com.duastore.dto.OrderDTO;
import com.duastore.dto.OrderItemDTO;
import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
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
