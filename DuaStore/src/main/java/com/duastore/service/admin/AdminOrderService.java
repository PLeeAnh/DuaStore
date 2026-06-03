package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminOrderService {

    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayDat"));
        return orderRepository.findAllBy(pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
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
}
