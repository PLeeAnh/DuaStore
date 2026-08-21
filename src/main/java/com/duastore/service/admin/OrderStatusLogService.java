package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderEventType;
import com.duastore.model.OrderStatusLog;
import com.duastore.model.User;
import com.duastore.repository.OrderStatusLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý đơn hàng, nhật ký hệ thống.
 */
public class OrderStatusLogService {

    private final OrderStatusLogRepository repository;

    public OrderStatusLogService(OrderStatusLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderStatusLog ghiLog(Order order, OrderEventType loaiSuKien, User nguoiThucHien,
            String trangThaiCu, String trangThaiMoi, String ghiChu) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrder(order);
        log.setLoaiSuKien(loaiSuKien);
        log.setNguoiThucHien(nguoiThucHien);
        log.setTrangThaiCu(trangThaiCu);
        log.setTrangThaiMoi(trangThaiMoi);
        log.setGhiChu(ghiChu);
        return repository.save(log);
    }

    @Transactional(readOnly = true)
    public List<OrderStatusLog> getLogsByOrder(Integer orderId) {
        return repository.findByOrderIdOrderByThoiGianAsc(orderId);
    }

    @Transactional
    public void deleteByOrderId(Integer orderId) {
        repository.deleteByOrderId(orderId);
    }
}
