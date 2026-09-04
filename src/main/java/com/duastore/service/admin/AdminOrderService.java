package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.OrderEventType;
import com.duastore.model.OrderItem;
import com.duastore.model.ProductVariant;
import com.duastore.model.User;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý đơn hàng.
 */
public class AdminOrderService {

    private static final Map<String, Set<String>> VALID_TRANSITIONS = new LinkedHashMap<>();
    private static final Map<String, String> STATUS_NAMES = new LinkedHashMap<>();
    /** Cac trang thai ma tai do ton kho THUC TE da bi tru (tu luc "Dang giao" tro di). */
    private static final Set<String> STOCK_DEDUCTED_STATES = Set.of("DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH");

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
    private final FlashSaleItemRepository flashSaleItemRepository;
    private final PricingService pricingService;
    private final com.duastore.service.client.OrderService clientOrderService;
    private final StockMovementService stockMovementService;

    public AdminOrderService(OrderRepository orderRepository,
            AdminLogService adminLogService,
            OrderAssignmentRepository assignmentRepository,
            OrderItemRepository orderItemRepository,
            ProductVariantRepository variantRepository,
            OrderStatusLogService orderStatusLogService,
            OrderNoteService orderNoteService,
            LoyaltyPointsService loyaltyPointsService,
            FlashSaleItemRepository flashSaleItemRepository,
            PricingService pricingService,
            com.duastore.service.client.OrderService clientOrderService,
            StockMovementService stockMovementService) {
        this.orderRepository = orderRepository;
        this.adminLogService = adminLogService;
        this.assignmentRepository = assignmentRepository;
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.orderNoteService = orderNoteService;
        this.loyaltyPointsService = loyaltyPointsService;
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.pricingService = pricingService;
        this.clientOrderService = clientOrderService;
        this.stockMovementService = stockMovementService;
    }

    @Transactional
    public Page<Order> getAllOrders(int page, int size, String q, String trangThai, String trangThaiTT,
            java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate, Boolean chuaGan, Integer assignedAdminId,
            String sortField, String sortDir) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC,
                sortField != null ? sortField : "ngayDat"
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepository.searchOrders(q, trangThai, trangThaiTT, fromDate, toDate, chuaGan, assignedAdminId, pageable);
        for (Order o : orders.getContent()) {
            adminLogService.tuDongPhanDon(o);
        }
        return orders;
    }

    @Transactional
    public Page<Order> getMyOrders(Integer adminId, int page, int size, String q, String trangThai, String trangThaiTT,
            java.time.LocalDateTime fromDate, java.time.LocalDateTime toDate, Boolean chuaGan, Integer assignedAdminId,
            String sortField, String sortDir) {
        List<OrderAssignment> assignments = assignmentRepository.findActiveByAdminId(adminId, "DANG_XU_LY");
        if (assignments.isEmpty()) {
            return Page.empty();
        }
        List<Integer> ids = assignments.stream()
                .map(a -> a.getOrder().getId())
                .collect(Collectors.toList());

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                "desc".equalsIgnoreCase(sortDir) ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC,
                sortField != null ? sortField : "ngayDat"
        );
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderRepository.searchOrdersByIds(ids, q, trangThai, trangThaiTT, fromDate, toDate, chuaGan, assignedAdminId, pageable);
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

    public void updatePaymentStatus(Integer id, String trangThaiTT) {
        Order order = getOrderById(id);
        String old = order.getTrangThaiTT();
        if (old == null || old.equals(trangThaiTT)) {
            throw new IllegalArgumentException("Trạng thái thanh toán không hợp lệ");
        }
        if ("DA_HOAN_THANH".equals(order.getTrangThaiDon()) && !"DA_THANH_TOAN".equals(trangThaiTT)) {
            throw new IllegalArgumentException("Đơn hàng đã hoàn thành, thanh toán phải là 'Đã thanh toán'");
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

    /**
     * Ton kho THUC TE (soLuongTon) chi bi tru khi don hang thuc su di giao — luc chuyen
     * sang trang thai "Dang giao" — chu khong tru ngay luc dat hang (xem OrderService).
     * Ly do: tranh tinh huong khach dat roi huy/khong xac nhan ma kho da bi tru nham,
     * trong khi don thuc su chi "mat hang" khi da xuat kho di giao.
     */
    private String adjustStock(Integer orderId, String newStatus, String oldStatus, String maDon, Integer adminUserId) {
        if ("DANG_GIAO".equals(newStatus)) {
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
                int affected = variantRepository.decrementStock(variant.getId(), item.getSoLuong());
                if (affected == 0) {
                    throw new IllegalArgumentException("Sản phẩm \"" + item.getTenSanPham()
                            + (item.getTenBienThe() != null ? " - " + item.getTenBienThe() : "")
                            + "\" không đủ hàng trong kho để giao");
                }
                if (adminUserId != null) {
                    stockMovementService.recordOut(item.getVariantId(), item.getSoLuong(), orderId, adminUserId,
                            "Xuất kho giao đơn " + maDon);
                }
                count += item.getSoLuong();
            }
            return count > 0 ? "Đã trừ " + count + " sản phẩm khỏi tồn kho." : null;
        }
        if ("DA_HUY".equals(newStatus)) {
            // Chi hoan lai ton kho neu don da tung bi tru that (tuc da qua "Dang giao").
            if (!STOCK_DEDUCTED_STATES.contains(oldStatus)) {
                return null;
            }
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
                if (adminUserId != null) {
                    stockMovementService.recordIn(item.getVariantId(), item.getSoLuong(), adminUserId,
                            "Hoàn kho do hủy đơn " + maDon);
                }
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
            String stockMsg = adjustStock(id, "DA_HUY", oldStatus, order.getMaDon(), admin != null ? admin.getId() : null);
            if (order.getUser() != null) {
                loyaltyPointsService.refundRedeemedPointsForOrder(order.getUser().getId(), id);
            }
            restoreFlashSaleQuota(id);
            clientOrderService.restoreVoucherForOrder(id);
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

        // Đơn hoàn thành bắt buộc thanh toán thành công
        if ("DA_HOAN_THANH".equals(trangThaiDon) && !"DA_THANH_TOAN".equals(order.getTrangThaiTT())) {
            String oldPayment = order.getTrangThaiTT();
            order.setTrangThaiTT("DA_THANH_TOAN");
            adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_TT",
                    oldPayment, "DA_THANH_TOAN",
                    "Xác nhận thanh toán khi hoàn thành đơn (admin xác nhận thay khách)", request);
        }

        if ("DA_GIAO".equals(trangThaiDon) && order.getNgayGiao() == null) {
            order.setNgayGiao(java.time.LocalDateTime.now());
        }

        // Tru ton kho TRUOC khi luu trang thai "Dang giao" — neu khong du hang, that bai
        // ngay tai day (khong doi trang thai don) thay vi luu roi moi bao loi.
        String stockMsg = adjustStock(id, trangThaiDon, oldStatus, order.getMaDon(), admin != null ? admin.getId() : null);

        order.setTrangThaiDon(trangThaiDon);
        orderRepository.save(order);

        if ("DA_HOAN_THANH".equals(trangThaiDon) && order.getUser() != null) {
            loyaltyPointsService.earnPoints(order.getUser().getId(), id, order.getTongThanhToan());
            clientOrderService.notifyOrderCompleted(order);
        }

        orderStatusLogService.ghiLog(order, OrderEventType.STATUS_CHANGE, admin, oldStatus, trangThaiDon, stockMsg);

        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_DON",
                oldStatus, trangThaiDon,
                "Cập nhật trạng thái đơn từ " + oldStatus + " → " + trangThaiDon, request);

        return stockMsg;
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

    public void updatePaymentStatusWithLog(Integer id, String trangThaiTT, String oldPayment,
            com.duastore.model.User admin, jakarta.servlet.http.HttpServletRequest request) {
        updatePaymentStatus(id, trangThaiTT);
        adminLogService.ghiLogDonHang(admin, id, "CAP_NHAT_TRANG_THAI_TT",
                oldPayment, trangThaiTT,
                "Cập nhật thanh toán từ " + oldPayment + " → " + trangThaiTT, request);
    }
}
