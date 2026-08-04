package com.duastore.service.admin;

import com.duastore.model.LoyaltyTransaction;
import com.duastore.model.Order;
import com.duastore.model.OrderEventType;
import com.duastore.model.RefundRequest;
import com.duastore.model.User;
import com.duastore.repository.LoyaltyTransactionRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.RefundRequestRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.LoyaltyPointsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusLogService orderStatusLogService;
    private final LoyaltyPointsService loyaltyPointsService;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    public RefundService(RefundRequestRepository refundRequestRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderStatusLogService orderStatusLogService,
            LoyaltyPointsService loyaltyPointsService,
            LoyaltyTransactionRepository loyaltyTransactionRepository) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderStatusLogService = orderStatusLogService;
        this.loyaltyPointsService = loyaltyPointsService;
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getAll() {
        return refundRequestRepository.findAllByOrderByNgayYeuCauDesc();
    }

    @Transactional(readOnly = true)
    public RefundRequest getById(Integer id) {
        return refundRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu hoàn tiền"));
    }

    @Transactional(readOnly = true)
    public boolean hasRefundRequestByOrderId(Integer orderId) {
        return refundRequestRepository.existsByOrderId(orderId);
    }

    public RefundRequest create(RefundRequest request) {
        return refundRequestRepository.save(request);
    }

    public RefundRequest approve(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"CHO_DUYET".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu hoàn tiền đã được xử lý");
        }
        request.setTrangThai("DA_DUYET");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        refundRequestRepository.save(request);

        // Update order status to DA_HOAN_TIEN to exclude it from revenue
        Integer orderId = request.getOrderId();
        if (orderId != null) {
            orderRepository.findById(orderId).ifPresent(order -> {
                String oldStatus = order.getTrangThaiDon();
                if ("DA_GIAO".equals(oldStatus) || "DA_HOAN_THANH".equals(oldStatus)) {
                    order.setTrangThaiDon("DA_HOAN_TIEN");
                    orderRepository.save(order);
                    User admin = userRepository.findById(adminId).orElse(null);
                    orderStatusLogService.ghiLog(order, OrderEventType.REFUND_ORDER, admin,
                            oldStatus, "DA_HOAN_TIEN", ghiChu);
                }
                if ("DA_HOAN_THANH".equals(oldStatus) && order.getUser() != null) {
                    deductLoyaltyPointsForRefund(order);
                }
            });
        }

        return request;
    }

    public RefundRequest reject(Integer id, Integer adminId, String ghiChu) {
        RefundRequest request = getById(id);
        if (!"CHO_DUYET".equals(request.getTrangThai())) {
            throw new RuntimeException("Yêu cầu hoàn tiền đã được xử lý");
        }
        request.setTrangThai("TU_CHOI");
        request.setNguoiXuLyId(adminId);
        request.setNgayXuLy(LocalDateTime.now());
        request.setGhiChuXuLy(ghiChu);
        return refundRequestRepository.save(request);
    }

    private void deductLoyaltyPointsForRefund(Order order) {
        int rate = loyaltyPointsService.getPointsEarnRate();
        int points = order.getTongThanhToan().divideToIntegralValue(java.math.BigDecimal.valueOf(rate)).intValue();
        if (points > 0) {
            loyaltyPointsService.adjustPoints(order.getUser().getId(), -points,
                    "Hoàn điểm từ đơn #" + order.getId() + " (hoàn tiền)", "Hệ thống");
        }
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return refundRequestRepository.countByTrangThai("CHO_DUYET");
    }

    @Transactional(readOnly = true)
    public long getCompletedCount(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return refundRequestRepository.countByTrangThaiAndNgayYeuCauBetween("DA_DUYET", start, end);
    }

    @Transactional(readOnly = true)
    public List<RefundRequest> getByUser(Integer userId) {
        return refundRequestRepository.findByUserIdOrderByNgayYeuCauDesc(userId);
    }
}
