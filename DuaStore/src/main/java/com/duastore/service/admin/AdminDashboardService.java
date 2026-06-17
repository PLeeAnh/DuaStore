package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.Product;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;

    public AdminDashboardService(ProductRepository productRepository,
                                  OrderRepository orderRepository,
                                  UserRepository userRepository,
                                  OrderAssignmentRepository orderAssignmentRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
    }

    public long getTotalProducts() {
        return productRepository.findDangBan().size();
    }

    public long getTodayOrders() {
        LocalDateTime start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return orderRepository.countByNgayDatBetween(start, end);
    }

    public String getMonthlyRevenue() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(now.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(now.withDayOfMonth(now.lengthOfMonth()), LocalTime.MAX);
        BigDecimal total = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(start, end);
        return formatVND(total);
    }

    public long getTotalCustomers() {
        return userRepository.countByRoleAndIsActiveTrue("USER");
    }

    public Map<String, Long> getOrderStatusCounts() {
        List<Object[]> rows = orderRepository.countGroupByTrangThaiDon();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("CHO_XAC_NHAN", 0L);
        map.put("DA_XAC_NHAN", 0L);
        map.put("DANG_GIAO", 0L);
        map.put("DA_GIAO", 0L);
        map.put("DA_HUY", 0L);
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByNgayDatDesc(PageRequest.of(0, 10));
    }

    public Map<Integer, String> getOrderAssignments(List<Order> orders) {
        Map<Integer, String> map = new HashMap<>();
        for (Order o : orders) {
            try {
                var ass = orderAssignmentRepository.findByOrderId(o.getId());
                ass.ifPresent(a -> map.put(o.getId(), a.getAdmin().getHoTen()));
            } catch (Exception ignored) {}
        }
        return map;
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0 ₫";
        long value = amount.longValue();
        if (value >= 1_000_000) {
            return String.format("%,d", value / 1_000_000) + "," + String.format("%03d", value % 1_000_000 / 1_000) + " triệu ₫";
        }
        return String.format("%,d ₫", value);
    }
}
