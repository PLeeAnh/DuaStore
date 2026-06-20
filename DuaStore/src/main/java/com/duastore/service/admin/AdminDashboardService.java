package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminDashboardService(ProductRepository productRepository,
                                  OrderRepository orderRepository,
                                  UserRepository userRepository,
                                  OrderAssignmentRepository orderAssignmentRepository,
                                  OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
        this.orderItemRepository = orderItemRepository;
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
        map.put("DA_HOAN_THANH", 0L);
        map.put("DA_HUY", 0L);
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    public Page<Order> getRecentOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByNgayDatDesc(pageable);
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

    public List<Map<String, Object>> getDailyRevenueLast7Days() {
        LocalDateTime since = LocalDateTime.now().minusDays(6).with(LocalTime.MIN);
        List<Order> orders = orderRepository.findCompletedOrdersSince(
                List.of("DA_GIAO", "DA_HOAN_THANH"), since);
        Map<LocalDate, BigDecimal> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyMap.put(LocalDate.now().minusDays(i), BigDecimal.ZERO);
        }
        for (Order o : orders) {
            LocalDate date = o.getNgayDat().toLocalDate();
            if (dailyMap.containsKey(date)) {
                dailyMap.put(date, dailyMap.get(date).add(o.getTongThanhToan()));
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dailyMap.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", entry.getKey().toString());
            row.put("revenue", entry.getValue());
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Order> orders = orderRepository.findCompletedOrdersSince(
                List.of("DA_GIAO", "DA_HOAN_THANH"), since);
        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
        for (Order o : orders) {
            var items = orderItemRepository.findByOrderId(o.getId());
            for (var item : items) {
                if (item.getProductId() == null) continue;
                productMap.computeIfAbsent(item.getProductId(), k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("productId", item.getProductId());
                    m.put("tenSanPham", item.getTenSanPham());
                    m.put("hinhAnh", item.getHinhAnhSP());
                    m.put("totalSold", 0);
                    return m;
                });
                productMap.get(item.getProductId()).merge("totalSold", item.getSoLuong(),
                        (a, b) -> (Integer) a + (Integer) b);
            }
        }
        return productMap.values().stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
