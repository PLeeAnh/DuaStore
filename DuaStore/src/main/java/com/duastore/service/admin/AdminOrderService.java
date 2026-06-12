package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final AdminLogService adminLogService;
    private final OrderAssignmentRepository assignmentRepository;

    public AdminOrderService(OrderRepository orderRepository,
                             AdminLogService adminLogService,
                             OrderAssignmentRepository assignmentRepository) {
        this.orderRepository = orderRepository;
        this.adminLogService = adminLogService;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllOrderByPriority(pageable);
        for (Order o : orders.getContent()) {
            adminLogService.tuDongPhanDon(o);
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public Page<Order> getMyOrders(Integer adminId, int page, int size) {
        List<OrderAssignment> assignments = assignmentRepository.findActiveByAdminId(adminId, "DANG_XU_LY");
        if (assignments.isEmpty()) {
            return Page.empty();
        }
        List<Integer> ids = assignments.stream()
                .map(a -> a.getOrder().getId())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByIdsWithPriority(ids, pageable);
    }

    @Transactional(readOnly = true)
    public long countPendingOrders() {
        return orderRepository.countByTrangThaiDon("CHO_XAC_NHAN");
    }

    @Transactional(readOnly = true)
    public long countMyPendingOrders(Integer adminId) {
        List<OrderAssignment> assignments = assignmentRepository.findActiveByAdminId(adminId, "DANG_XU_LY");
        if (assignments.isEmpty()) return 0;
        List<Integer> ids = assignments.stream()
                .map(a -> a.getOrder().getId())
                .collect(java.util.stream.Collectors.toList());
        return orderRepository.countByTrangThaiDonAndIdIn("CHO_XAC_NHAN", ids);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        adminLogService.tuDongPhanDon(order);
        return order;
    }

    public void updateOrderStatus(Integer id, String trangThaiDon) {
        Order order = getOrderById(id);
        order.setTrangThaiDon(trangThaiDon);
        orderRepository.save(order);
    }

    public void updatePaymentStatus(Integer id, String trangThaiTT) {
        Order order = getOrderById(id);
        order.setTrangThaiTT(trangThaiTT);
        orderRepository.save(order);
    }

    public void updateOrderStatusWithLog(Integer id, String trangThaiDon, String oldStatus,
                                          com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        updateOrderStatus(id, trangThaiDon);
        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_DON",
                oldStatus, trangThaiDon,
                "Cập nhật trạng thái đơn từ " + oldStatus + " → " + trangThaiDon, request);
    }

    public void updatePaymentStatusWithLog(Integer id, String trangThaiTT, String oldPayment,
                                            com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        updatePaymentStatus(id, trangThaiTT);
        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_TT",
                oldPayment, trangThaiTT,
                "Cập nhật thanh toán từ " + oldPayment + " → " + trangThaiTT, request);
    }
}
