package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.OrderAssignment;
import com.duastore.model.OrderItem;
import com.duastore.model.Product;
import com.duastore.repository.OrderAssignmentRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserRepository;
import com.duastore.util.PriceUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý bảng điều khiển (dashboard).
 */
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderAssignmentRepository orderAssignmentRepository;
    private final OrderItemRepository orderItemRepository;
    private final PromotionRepository promotionRepository;
    private final ProductVariantRepository productVariantRepository;

    public AdminDashboardService(ProductRepository productRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderAssignmentRepository orderAssignmentRepository,
            OrderItemRepository orderItemRepository,
            PromotionRepository promotionRepository,
            ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderAssignmentRepository = orderAssignmentRepository;
        this.orderItemRepository = orderItemRepository;
        this.promotionRepository = promotionRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public long getTotalProducts() {
        return productRepository.findDangBan().size();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public long getActivePromotions() {
        return promotionRepository.findActiveNow(LocalDateTime.now()).size();
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
        if (orders == null || orders.isEmpty()) {
            return Map.of();
        }
        List<Integer> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderAssignment> assignments = orderAssignmentRepository.findByOrderIdIn(orderIds);
        Map<Integer, String> map = new HashMap<>();
        for (OrderAssignment a : assignments) {
            map.put(a.getOrder().getId(), a.getAdmin().getHoTen());
        }
        return map;
    }

    private String formatVND(BigDecimal amount) {
        if (amount == null) return "0₫";
        long value = amount.longValue();
        if (value >= 1_000_000) {
            return String.format("%,d", value / 1_000_000) + "," + String.format("%03d", value % 1_000_000 / 1_000) + " triệu₫";
        }
        return PriceUtils.format(amount);
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
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Integer> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemRepository.findByOrderIdIn(orderIds);

        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
        for (var item : items) {
            if (item.getProductId() == null) {
                continue;
            }
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
        return productMap.values().stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== ENHANCED DASHBOARD COMPARISONS ====================
    public Map<String, Object> getStatComparison() {
        Map<String, Object> result = new LinkedHashMap<>();

        // Yesterday vs today (for todayOrders)
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayOrders = orderRepository.countByNgayDatBetween(
                today.atStartOfDay(), today.atTime(LocalTime.MAX));
        long yesterdayOrders = orderRepository.countByNgayDatBetween(
                yesterday.atStartOfDay(), yesterday.atTime(LocalTime.MAX));
        result.put("todayOrders", todayOrders);
        result.put("todayOrdersChange", calcChange(todayOrders, yesterdayOrders));

        // This month vs last month (for totalOrders, monthlyRevenue, newCustomers)
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfLastMonth = firstOfMonth.minusMonths(1);
        LocalDate lastOfLastMonth = firstOfMonth.minusDays(1);

        long ordersThisMonth = orderRepository.countByNgayDatBetween(firstOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX));
        long ordersLastMonth = orderRepository.countByNgayDatBetween(firstOfLastMonth.atStartOfDay(), lastOfLastMonth.atTime(LocalTime.MAX));
        result.put("ordersThisMonth", ordersThisMonth);
        result.put("ordersChange", calcChange(ordersThisMonth, ordersLastMonth));

        BigDecimal revenueThisMonth = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(
                firstOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX));
        BigDecimal revenueLastMonth = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(
                firstOfLastMonth.atStartOfDay(), lastOfLastMonth.atTime(LocalTime.MAX));
        result.put("revenueThisMonth", formatVND(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO));
        result.put("revenueChange", calcChange(
                revenueThisMonth != null ? revenueThisMonth.longValue() : 0L,
                revenueLastMonth != null ? revenueLastMonth.longValue() : 0L));

        long newCustomersThisMonth = userRepository.countByNgayTaoBetween(
                firstOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX));
        long newCustomersLastMonth = userRepository.countByNgayTaoBetween(
                firstOfLastMonth.atStartOfDay(), lastOfLastMonth.atTime(LocalTime.MAX));
        result.put("newCustomersThisMonth", newCustomersThisMonth);
        result.put("customersChange", calcChange(newCustomersThisMonth, newCustomersLastMonth));

        return result;
    }

    public List<Map<String, Object>> getPreviousWeekRevenue() {
        LocalDate today = LocalDate.now();
        LocalDate end = today.minusDays(7);
        LocalDate start = end.minusDays(6);
        List<Order> orders = orderRepository.findCompletedOrdersSince(
                List.of("DA_GIAO", "DA_HOAN_THANH"), start.atStartOfDay());
        Map<LocalDate, BigDecimal> dailyMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dailyMap.put(end.minusDays(6 - i), BigDecimal.ZERO);
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

    public Map<String, Long> getPaymentMethodDistribution() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now().plusDays(1).with(LocalTime.MAX);
        List<Object[]> rows = orderRepository.countGroupByPhuongThucTTAndNgayDatBetween(start, end);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String method = (String) row[0];
            Long count = (Long) row[1];
            map.put(method != null ? method : "UNKNOWN", count);
        }
        return map;
    }

    public List<Map<String, Object>> getSalesFunnel() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now().plusDays(1).with(LocalTime.MAX);
        List<Map<String, Object>> funnel = new ArrayList<>();
        Map<String, String> stageLabels = Map.of(
                "CHO_XAC_NHAN", "Chờ xác nhận",
                "DA_XAC_NHAN", "Đã xác nhận",
                "DANG_GIAO", "Đang giao",
                "DA_GIAO", "Đã giao",
                "DA_HOAN_THANH", "Hoàn thành");
        long prevCount = 0;
        for (String status : List.of("CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH")) {
            long count = orderRepository.countByTrangThaiDonAndNgayDatBetween(status, start, end);
            Map<String, Object> stage = new LinkedHashMap<>();
            stage.put("status", status);
            stage.put("label", stageLabels.get(status));
            stage.put("count", count);
            stage.put("widthPct", prevCount > 0 ? (count * 100 / prevCount) : 100);
            stage.put("convRate", prevCount > 0 ? (count * 100.0 / prevCount) : 100.0);
            stage.put("isFirst", prevCount == 0);
            stage.put("color", getFunnelColor(status));
            funnel.add(stage);
            prevCount = count;
        }
        return funnel;
    }

    private String getFunnelColor(String status) {
        return switch (status) {
            case "CHO_XAC_NHAN" ->
                "#4f46e5";
            case "DA_XAC_NHAN" ->
                "#0ea5e9";
            case "DANG_GIAO" ->
                "#f59e0b";
            case "DA_GIAO" ->
                "#10b981";
            case "DA_HOAN_THANH" ->
                "#6366f1";
            default ->
                "#6b7280";
        };
    }

    public String getRevenueGrowth() {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        LocalDate firstOfLastMonth = firstOfMonth.minusMonths(1);
        LocalDate lastOfLastMonth = firstOfMonth.minusDays(1);

        BigDecimal current = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(
                firstOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX));
        BigDecimal previous = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(
                firstOfLastMonth.atStartOfDay(), lastOfLastMonth.atTime(LocalTime.MAX));

        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? "+100%" : "0%";
        }
        BigDecimal growth = current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
        return (growth.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + growth + "%";
    }

    public long getLowStockCount() {
        return productVariantRepository.countLowStockProducts();
    }

    public List<Map<String, Object>> getLowStockProducts(int limit) {
        List<Object[]> rows = productVariantRepository.findLowStockProductIds();
        List<Integer> ids = rows.stream().map(r -> (Integer) r[0]).collect(Collectors.toList());
        if (ids.isEmpty()) return List.of();
        Map<Integer, Long> stockMap = new HashMap<>();
        for (Object[] r : rows) {
            stockMap.put((Integer) r[0], (Long) r[1]);
        }
        // Use findAllById to fetch all products in one query
        List<Product> products = productRepository.findAllById(ids);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Product p : products) {
            if (!p.isActive() || !stockMap.containsKey(p.getId())) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("productId", p.getId());
            m.put("tenSanPham", p.getTenSanPham());
            m.put("hinhAnh", p.getHinhAnhChinh());
            m.put("totalStock", stockMap.get(p.getId()));
            resultList.add(m);
        }
        resultList.sort((a, b) -> Long.compare((Long) a.get("totalStock"), (Long) b.get("totalStock")));
        return resultList.stream().limit(limit).collect(Collectors.toList());
    }

    public long getUrgentOrderCount() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        return orderRepository.countByTrangThaiDonAndNgayDatBefore("CHO_XAC_NHAN", threshold);
    }

    public List<Map<String, Object>> getMonthlyRevenueLast12Months() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            java.time.YearMonth ym = java.time.YearMonth.from(now.minusMonths(i));
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
            BigDecimal rev = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(start, end);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", ym.getMonthValue());
            row.put("year", ym.getYear());
            row.put("label", "T" + ym.getMonthValue() + "/" + String.valueOf(ym.getYear()).substring(2));
            row.put("revenue", rev != null ? rev : BigDecimal.ZERO);
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> getTopSellingProductsLast7Days(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Order> orders = orderRepository.findCompletedOrdersSince(
                List.of("DA_GIAO", "DA_HOAN_THANH"), since);
        if (orders.isEmpty()) {
            return List.of();
        }
        List<Integer> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> items = orderItemRepository.findByOrderIdIn(orderIds);

        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
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
        return productMap.values().stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCancelRefundRate() {
        long total = orderRepository.count();
        long cancelled = orderRepository.countByTrangThaiDon("DA_HUY");
        long refunded = orderRepository.countByTrangThaiDon("DA_HOAN_TIEN");
        long bad = cancelled + refunded;
        String rate = total > 0 ? String.format("%.1f%%", (bad * 100.0 / total)) : "0%";
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("cancelled", cancelled);
        m.put("refunded", refunded);
        m.put("rate", rate);
        return m;
    }

    private String calcChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double change = ((double) (current - previous) / previous) * 100;
        return (change >= 0 ? "+" : "") + String.format("%.1f", change) + "%";
    }

}
