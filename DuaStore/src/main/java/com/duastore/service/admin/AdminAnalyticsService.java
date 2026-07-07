package com.duastore.service.admin;

import com.duastore.model.Order;
import com.duastore.model.ProductVariant;
import com.duastore.model.VoucherStatus;
import com.duastore.repository.*;
import com.duastore.util.PriceUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminAnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final UserVoucherRepository userVoucherRepository;

    public AdminAnalyticsService(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            UserRepository userRepository,
            PromotionRepository promotionRepository,
            UserVoucherRepository userVoucherRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.promotionRepository = promotionRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    // ==================== REVENUE ====================
    public List<Map<String, Object>> getDailyRevenue(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository.findByTrangThaiDonInAndNgayDatBetween(
                List.of("DA_GIAO", "DA_HOAN_THANH"), start, end);

        Map<LocalDate, BigDecimal> dailyMap = new LinkedHashMap<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            dailyMap.put(current, BigDecimal.ZERO);
            current = current.plusDays(1);
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

    public String getTotalRevenue(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        BigDecimal total = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(start, end);
        return PriceUtils.format(total != null ? total : BigDecimal.ZERO);
    }

    public List<Map<String, Object>> getRevenueByCategory(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository.findByTrangThaiDonInAndNgayDatBetween(
                List.of("DA_GIAO", "DA_HOAN_THANH"), start, end);

        Map<Integer, BigDecimal> categoryRevenue = new LinkedHashMap<>();
        for (Order o : orders) {
            var items = orderItemRepository.findByOrderId(o.getId());
            for (var item : items) {
                if (item.getProductId() == null) {
                    continue;
                }
                var product = productRepository.findById(item.getProductId());
                if (product.isPresent()) {
                    Integer catId = product.get().getDanhMucId();
                    BigDecimal amount = BigDecimal.valueOf(item.getSoLuong()).multiply(item.getDonGia());
                    categoryRevenue.merge(catId, amount, BigDecimal::add);
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        categoryRevenue.entrySet().stream()
                .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", "Danh mục #" + e.getKey());
                    row.put("revenue", e.getValue());
                    result.add(row);
                });
        return result;
    }

    // ==================== ORDERS ====================
    public Map<String, Long> getOrderStatusCounts(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        Map<String, Long> map = new LinkedHashMap<>();
        for (String status : List.of("CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH", "DA_HUY")) {
            map.put(status, orderRepository.countByTrangThaiDonAndNgayDatBetween(status, start, end));
        }
        return map;
    }

    public Map<String, Long> getPaymentMethodCounts(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Object[]> rows = orderRepository.countGroupByPhuongThucTTAndNgayDatBetween(start, end);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String method = (String) row[0];
            Long count = (Long) row[1];
            map.put(method != null ? method : "UNKNOWN", count);
        }
        return map;
    }

    public String getAvgOrderValue(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        BigDecimal avg = orderRepository.avgTongThanhToanByNgayDatBetween(start, end);
        return PriceUtils.format(avg != null ? avg : BigDecimal.ZERO);
    }

    public long getTotalOrders(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return orderRepository.countByNgayDatBetween(start, end);
    }

    public long getCompletedOrders(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return orderRepository.countByTrangThaiDonAndNgayDatBetween("DA_HOAN_THANH", start, end)
                + orderRepository.countByTrangThaiDonAndNgayDatBetween("DA_GIAO", start, end);
    }

    public long getCancelledOrders(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return orderRepository.countByTrangThaiDonAndNgayDatBetween("DA_HUY", start, end);
    }

    public long getPaymentCount(String paymentMethod, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return orderRepository.countByPhuongThucTTAndNgayDatBetween(paymentMethod, start, end);
    }

    public long getOnlineOrderCount(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return orderRepository.countByPhuongThucTTAndNgayDatBetween("BANK_TRANSFER", start, end)
                + orderRepository.countByPhuongThucTTAndNgayDatBetween("MOMO", start, end)
                + orderRepository.countByPhuongThucTTAndNgayDatBetween("VNPAY", start, end);
    }

    public String getCompletionRate(LocalDate from, LocalDate to) {
        long total = getTotalOrders(from, to);
        if (total == 0) {
            return "0%";
        }
        long completed = getCompletedOrders(from, to);
        double rate = (completed * 100.0) / total;
        return String.format("%.1f%%", rate);
    }

    // ==================== CUSTOMERS ====================
    public long getNewCustomers(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return userRepository.countByNgayTaoBetween(start, end);
    }

    public List<Map<String, Object>> getTopCustomers(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Object[]> rows = userRepository.findTopCustomersByRevenue(start, end, PageRequest.of(0, 10));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", row[0]);
            m.put("hoTen", row[1]);
            m.put("orderCount", row[2]);
            m.put("totalSpent", row[3]);
            result.add(m);
        }
        return result;
    }

    public long getTotalCustomers() {
        return userRepository.count();
    }

    public String getAvgRevenuePerCustomer(LocalDate from, LocalDate to) {
        long customers = getNewCustomers(from, to);
        if (customers == 0) {
            return "0₫";
        }
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        BigDecimal total = orderRepository.sumTongThanhToanByTrangThaiDonAndNgayDatBetween(start, end);
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        return PriceUtils.format(total.divide(BigDecimal.valueOf(customers), 0, RoundingMode.HALF_UP));
    }

    // ==================== PRODUCTS ====================
    public List<Map<String, Object>> getTopSellingProducts(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository.findByTrangThaiDonInAndNgayDatBetween(
                List.of("DA_GIAO", "DA_HOAN_THANH"), start, end);

        Map<Integer, Map<String, Object>> productMap = new LinkedHashMap<>();
        for (Order o : orders) {
            var items = orderItemRepository.findByOrderId(o.getId());
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
                    m.put("revenue", BigDecimal.ZERO);
                    return m;
                });
                Map<String, Object> m = productMap.get(item.getProductId());
                m.put("totalSold", (Integer) m.get("totalSold") + item.getSoLuong());
                m.put("revenue", ((BigDecimal) m.get("revenue")).add(
                        BigDecimal.valueOf(item.getSoLuong()).multiply(item.getDonGia())));
            }
        }
        return productMap.values().stream()
                .sorted((a, b) -> Integer.compare((Integer) b.get("totalSold"), (Integer) a.get("totalSold")))
                .limit(10)
                .collect(Collectors.toList());
    }

    public long getLowStockProducts() {
        return productVariantRepository.findAll().stream()
                .filter(v -> v.isActive() && v.getSoLuongTon() != null && v.getSoLuongTon() < 5)
                .count();
    }

    public long getTotalStock() {
        return productVariantRepository.findAll().stream()
                .filter(v -> v.getSoLuongTon() != null)
                .mapToLong(ProductVariant::getSoLuongTon)
                .sum();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }

    // ==================== PROMOTIONS ====================
    public Map<String, Long> getVoucherStats() {
        List<Object[]> rows = userVoucherRepository.countGroupByStatus();
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("AVAILABLE", 0L);
        map.put("USED", 0L);
        map.put("EXPIRED", 0L);
        for (Object[] row : rows) {
            String status = row[0] instanceof VoucherStatus ? ((VoucherStatus) row[0]).name() : (String) row[0];
            Long count = (Long) row[1];
            map.put(status, count);
        }
        return map;
    }

    public List<Map<String, Object>> getTopVouchers() {
        List<Object[]> rows = userVoucherRepository.countUsedGroupByPromotionId(PageRequest.of(0, 10));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Integer promoId = (Integer) row[0];
            Long usedCount = (Long) row[1];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("promotionId", promoId);
            m.put("usedCount", usedCount);
            var promo = promotionRepository.findById(promoId);
            promo.ifPresent(p -> {
                m.put("maCode", p.getMaCode());
                m.put("tenChuongTrinh", p.getTenChuongTrinh());
                m.put("giaTriGiam", p.getGiaTriGiam());
                m.put("loaiGiam", p.getLoaiGiam());
            });
            result.add(m);
        }
        return result;
    }

    public long getActivePromotions() {
        return promotionRepository.findActiveNow(LocalDateTime.now()).size();
    }

    public String getTotalDiscountGiven(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository.findByTrangThaiDonInAndNgayDatBetween(
                List.of("DA_GIAO", "DA_HOAN_THANH"), start, end);
        BigDecimal totalDiscount = orders.stream()
                .map(Order::getTienGiam)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return PriceUtils.format(totalDiscount);
    }

    // ==================== RECENT ORDERS ====================
    public List<Map<String, Object>> getRecentOrders(int limit) {
        List<Order> orders = orderRepository.findTop10ByOrderByNgayDatDesc(PageRequest.of(0, limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("maDon", o.getMaDon());
            m.put("tenNguoiNhan", o.getSnapTenNguoiNhan());
            m.put("tongThanhToan", o.getTongThanhToan());
            m.put("trangThaiDon", o.getTrangThaiDon());
            m.put("phuongThucTT", o.getPhuongThucTT());
            m.put("ngayDat", o.getNgayDat());
            result.add(m);
        }
        return result;
    }
}
