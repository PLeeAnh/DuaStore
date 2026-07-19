package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.OrderEventType;
import com.duastore.model.OrderItem;
import com.duastore.model.ProductVariant;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.LoyaltyPointsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminOrderService {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = new LinkedHashMap<>();
    private static final Map<String, String> STATUS_NAMES = new LinkedHashMap<>();

    static {
        VALID_TRANSITIONS.put("CHO_XAC_NHAN", Set.of("DA_XAC_NHAN", "DA_HUY"));
        VALID_TRANSITIONS.put("DA_XAC_NHAN", Set.of("DANG_GIAO", "DA_HUY"));
        VALID_TRANSITIONS.put("DANG_GIAO", Set.of("DA_GIAO", "DA_HUY"));
        VALID_TRANSITIONS.put("DA_GIAO", Set.of("DA_HOAN_THANH", "DA_HUY"));
        VALID_TRANSITIONS.put("DA_HOAN_THANH", Set.of());
        VALID_TRANSITIONS.put("DA_HUY", Set.of());

        STATUS_NAMES.put("CHO_XAC_NHAN", "Chờ xác nhận");
        STATUS_NAMES.put("DA_XAC_NHAN", "Đã xác nhận");
        STATUS_NAMES.put("DANG_GIAO", "Đang giao");
        STATUS_NAMES.put("DA_GIAO", "Đã giao");
        STATUS_NAMES.put("DA_HOAN_THANH", "Đã hoàn thành");
        STATUS_NAMES.put("DA_HUY", "Đã hủy");
    }

    private final OrderRepository orderRepository;
    private final AdminLogService adminLogService;
    private final OrderAssignmentRepository assignmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderStatusLogService orderStatusLogService;
    private final OrderNoteService orderNoteService;
    private final LoyaltyPointsService loyaltyPointsService;

    public AdminOrderService(OrderRepository orderRepository,
            AdminLogService adminLogService,
            OrderAssignmentRepository assignmentRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository variantRepository,
            OrderStatusLogService orderStatusLogService,
            OrderNoteService orderNoteService,
            LoyaltyPointsService loyaltyPointsService) {
        this.orderRepository = orderRepository;
        this.adminLogService = adminLogService;
        this.assignmentRepository = assignmentRepository;
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.orderNoteService = orderNoteService;
        this.loyaltyPointsService = loyaltyPointsService;
    }

    @Transactional
    public Page<Order> getAllOrders(int page, int size, String q, String trangThai, String trangThaiTT) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.searchOrders(q, trangThai, trangThaiTT, pageable);
        for (Order o : orders.getContent()) {
            adminLogService.tuDongPhanDon(o);
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public Page<Order> getMyOrders(Integer adminId, int page, int size, String q, String trangThai, String trangThaiTT) {
        List<OrderAssignment> assignments = assignmentRepository.findActiveByAdminId(adminId, "DANG_XU_LY");
        if (assignments.isEmpty()) {
            return Page.empty();
        }
        List<Integer> ids = assignments.stream()
                .map(a -> a.getOrder().getId())
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.searchOrdersByIds(ids, q, trangThai, trangThaiTT, pageable);
    }

    @Transactional(readOnly = true)
    public long countPendingOrders() {
        return orderRepository.countByTrangThaiDon("CHO_XAC_NHAN");
    }

    @Transactional(readOnly = true)
    public long countMyPendingOrders(Integer adminId) {
        List<OrderAssignment> assignments = assignmentRepository.findActiveByAdminId(adminId, "DANG_XU_LY");
        if (assignments.isEmpty()) {
            return 0;
        }
        List<Integer> ids = assignments.stream()
                .map(a -> a.getOrder().getId())
                .collect(java.util.stream.Collectors.toList());
        return orderRepository.countByTrangThaiDonAndIdIn("CHO_XAC_NHAN", ids);
    }

    @Transactional
    public Order getOrderById(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        adminLogService.tuDongPhanDon(order);
        return order;
    }

    public void updateOrderStatus(Integer id, String trangThaiDon) {
        Order order = getOrderById(id);
        String error = validateTransition(order.getTrangThaiDon(), trangThaiDon);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        order.setTrangThaiDon(trangThaiDon);
        orderRepository.save(order);
    }

    public void updatePaymentStatus(Integer id, String trangThaiTT) {
        Order order = getOrderById(id);
        String old = order.getTrangThaiTT();
        if (old == null || old.equals(trangThaiTT)) {
            throw new IllegalArgumentException("Trạng thái thanh toán không hợp lệ");
        }
        order.setTrangThaiTT(trangThaiTT);
        orderRepository.save(order);
    }

    public static Set<String> getValidNextStatuses(String currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }

    public static String getStatusName(String code) {
        return STATUS_NAMES.getOrDefault(code, code);
    }

    public String validateTransition(String oldStatus, String newStatus) {
        if (oldStatus == null || newStatus == null) {
            return "Trạng thái không hợp lệ";
        }
        if (oldStatus.equals(newStatus)) {
            return "Trạng thái mới phải khác trạng thái hiện tại";
        }
        Set<String> allowed = getValidNextStatuses(oldStatus);
        if (!allowed.contains(newStatus)) {
            return "Không thể chuyển từ " + getStatusName(oldStatus) + " sang " + getStatusName(newStatus);
        }
        return null;
    }

    private String adjustStock(Integer orderId, String newStatus, String oldStatus) {
        if ("DA_HUY".equals(newStatus)) {
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            int count = 0;
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
                count += item.getSoLuong();
            }
            return count > 0 ? "Đã hoàn lại " + count + " sản phẩm vào tồn kho." : null;
        }
        return null;
    }

    public String updateOrderStatusWithLog(Integer id, String trangThaiDon, String oldStatus,
            com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        String error = validateTransition(oldStatus, trangThaiDon);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        if ("DA_HUY".equals(trangThaiDon)) {
            String stockMsg = adjustStock(id, "DA_HUY", oldStatus);
            order.setTrangThaiDon("DA_HUY");
            orderRepository.save(order);
            orderStatusLogService.ghiLog(order, OrderEventType.CANCEL_ORDER, admin, oldStatus, "DA_HUY",
                    "Đã hủy đơn (trạng thái cũ: " + oldStatus + ")" + (stockMsg != null ? ". " + stockMsg : ""));
            adminLogService.ghiLogDonHang(admin, id, "HUY_DON_HANG",
                    oldStatus, "DA_HUY",
                    "Hủy đơn hàng (trạng thái cũ: " + oldStatus + ")" + (stockMsg != null ? ". " + stockMsg : ""),
                    request);
            return stockMsg;
        }

        // Auto-set payment to DA_THANH_TOAN when completing unpaid order
        if ("DA_HOAN_THANH".equals(trangThaiDon) && "CHUA_THANH_TOAN".equals(order.getTrangThaiTT())) {
            String oldPayment = order.getTrangThaiTT();
            order.setTrangThaiTT("DA_THANH_TOAN");
            adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_TT",
                    oldPayment, "DA_THANH_TOAN",
                    "Xác nhận thanh toán khi hoàn thành đơn (admin xác nhận thay khách)", request);
        }

        order.setTrangThaiDon(trangThaiDon);
        orderRepository.save(order);

        if ("DA_HOAN_THANH".equals(trangThaiDon) && order.getUser() != null) {
            loyaltyPointsService.earnPoints(order.getUser().getId(), id, order.getTongThanhToan());
        }

        String stockMsg = adjustStock(id, trangThaiDon, oldStatus);
        orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin, oldStatus, trangThaiDon, stockMsg);

        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_DON",
                oldStatus, trangThaiDon,
                "Cập nhật trạng thái đơn từ " + oldStatus + " → " + trangThaiDon, request);

        return stockMsg;
    }

    public String deleteOrderWithLog(Integer id, String oldStatus,
            com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        String stockMsg = adjustStock(id, "DA_HUY", oldStatus);

        Order order = orderRepository.findById(id).orElse(null);
        orderStatusLogService.ghiLog(order, OrderEventType.CANCEL_ORDER, admin, oldStatus, null,
                "Đã hủy đơn (trạng thái cũ: " + oldStatus + ")" + (stockMsg != null ? ". " + stockMsg : ""));

        adminLogService.ghiLogDonHang(admin, id, "XOA_DON_HANG",
                oldStatus, null,
                "Xóa đơn hàng (trạng thái cũ: " + oldStatus + ")" + (stockMsg != null ? ". " + stockMsg : ""),
                request);
        assignmentRepository.findByOrderId(id).ifPresent(assignmentRepository::delete);
        orderStatusLogService.deleteByOrderId(id);
        orderNoteService.deleteByOrderId(id);
        orderRepository.deleteById(id);
        return stockMsg;
    }

    public void updatePaymentStatusWithLog(Integer id, String trangThaiTT, String oldPayment,
            com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        updatePaymentStatus(id, trangThaiTT);
        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_TT",
                oldPayment, trangThaiTT,
                "Cập nhật thanh toán từ " + oldPayment + " → " + trangThaiTT, request);
    }
}
