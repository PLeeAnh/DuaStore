package com.duastore.scheduler;

import com.duastore.model.Order;
import com.duastore.repository.OrderRepository;
import com.duastore.service.SiteSettingService;
import com.duastore.service.client.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
/**
 * Tác vụ chạy định kỳ (scheduled job) xử lý đơn hàng.
 */
public class OrderAutoCancelScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final SiteSettingService siteSettingService;

    public OrderAutoCancelScheduler(OrderRepository orderRepository,
            OrderService orderService,
            SiteSettingService siteSettingService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.siteSettingService = siteSettingService;
    }

    @Scheduled(fixedDelay = 600000, initialDelay = 120000)
    public void autoCancelPendingOrders() {
        int hours = getAutoCancelHours();
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        List<Order> staleOrders = orderRepository.findPendingUnpaidOrdersBefore("CHO_XAC_NHAN", threshold);
        for (Order order : staleOrders) {
            try {
                orderService.cancelOrder(order.getUser().getId(), order.getId(),
                        "Tự động hủy do quá " + hours + " giờ chưa xác nhận thanh toán");
                log.info("Da tu dong huy don {} (quá {}h)", order.getMaDon(), hours);
            } catch (Exception e) {
                log.warn("Khong the tu dong huy don {}: {}", order.getMaDon(), e.getMessage());
            }
        }
    }

    private int getAutoCancelHours() {
        try {
            return Integer.parseInt(siteSettingService.getValue("order_auto_cancel_hours", "24"));
        } catch (NumberFormatException e) {
            return 24;
        }
    }
}
