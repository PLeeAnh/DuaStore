package com.duastore.service;

import com.duastore.dto.PricingSuggestionDTO;
import com.duastore.model.ProductVariant;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DynamicPricingService {

    private final ProductVariantRepository variantRepository;
    private final OrderItemRepository orderItemRepository;

    public DynamicPricingService(ProductVariantRepository variantRepository,
                                  OrderItemRepository orderItemRepository) {
        this.variantRepository = variantRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public PricingSuggestionDTO suggestForVariant(Integer variantId) {
        ProductVariant v = variantRepository.findById(variantId).orElse(null);
        if (v == null) return null;

        long sold = getSoldInDays(variantId, 30);
        int stock = v.getSoLuongTon() != null ? v.getSoLuongTon() : 0;
        double salesPerDay = sold / 30.0;
        int daysUntilEmpty = salesPerDay > 0 ? (int) Math.round(stock / salesPerDay) : 999;

        // Calculate all-variant average sales per day for comparison
        double avgSalesPerDay = getAverageSalesPerDay(30);

        // Scores
        int inventoryScore = calcInventoryScore(daysUntilEmpty);
        int velocityScore = calcVelocityScore(salesPerDay, avgSalesPerDay);
        int combined = (int) Math.round(inventoryScore * 0.5 + velocityScore * 0.5);

        PricingSuggestionDTO dto = new PricingSuggestionDTO();
        dto.setVariantId(v.getId());
        dto.setVariantName(v.getTenBienThe());
        dto.setProductId(v.getProductId());
        dto.setProductName(v.getProduct() != null ? v.getProduct().getTenSanPham() : "");
        dto.setCurrentGiaGoc(v.getGiaGoc());
        dto.setCurrentGiaKhuyenMai(v.getGiaKhuyenMai());
        dto.setCurrentStock(stock);
        dto.setSalesPerDay(Math.round(salesPerDay * 10.0) / 10.0);
        dto.setDaysUntilEmpty(daysUntilEmpty);
        dto.setSeasonInfo("Chưa có dữ liệu mùa vụ");

        // Map combined score to action
        if (combined >= 70) {
            // Hot product with low stock → increase price
            dto.setSuggestedAction("INCREASE_PRICE");
            BigDecimal increase = v.getGiaGoc().multiply(new BigDecimal("1.10")).setScale(0, RoundingMode.DOWN);
            dto.setSuggestedGiaGoc(increase);
            dto.setSuggestedGiaKhuyenMai(v.getGiaKhuyenMai());
            dto.setSuggestedDiscountPct(null);
            dto.setReason("Sản phẩm bán chạy, tồn kho sắp hết (" + daysUntilEmpty + " ngày). Có thể tăng giá.");
            dto.setConfidence(daysUntilEmpty < 7 ? "HIGH" : "MEDIUM");
            dto.setActionable(true);
        } else if (combined <= 30) {
            // Slow mover with high stock → offer discount
            BigDecimal discountPct = new BigDecimal("15");
            BigDecimal salePrice = v.getGiaGoc()
                    .multiply(BigDecimal.ONE.subtract(discountPct.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)))
                    .setScale(0, RoundingMode.HALF_UP);
            dto.setSuggestedAction("OFFER_DISCOUNT");
            dto.setSuggestedGiaGoc(v.getGiaGoc());
            dto.setSuggestedGiaKhuyenMai(salePrice);
            dto.setSuggestedDiscountPct(15);
            dto.setReason("Tồn kho cao (" + stock + "), tốc độ bán chậm. Nên giảm giá " + 15 + "% để kích cầu.");
            dto.setConfidence("MEDIUM");
            dto.setActionable(true);
        } else if (combined <= 45 && stock > 30) {
            // Slightly slow, offer mild discount
            BigDecimal salePrice = v.getGiaGoc()
                    .multiply(new BigDecimal("0.93")).setScale(0, RoundingMode.HALF_UP);
            dto.setSuggestedAction("OFFER_DISCOUNT");
            dto.setSuggestedGiaGoc(v.getGiaGoc());
            dto.setSuggestedGiaKhuyenMai(salePrice);
            dto.setSuggestedDiscountPct(7);
            dto.setReason("Tồn kho còn " + stock + ", có thể áp dụng khuyến mãi nhẹ 7%.");
            dto.setConfidence("LOW");
            dto.setActionable(true);
        } else {
            dto.setSuggestedAction("NO_CHANGE");
            dto.setSuggestedGiaGoc(v.getGiaGoc());
            dto.setSuggestedGiaKhuyenMai(v.getGiaKhuyenMai());
            dto.setSuggestedDiscountPct(0);
            dto.setReason("Giá hiện tại đã hợp lý với tình hình tồn kho và doanh số.");
            dto.setConfidence("HIGH");
            dto.setActionable(false);
        }

        return dto;
    }

    public List<PricingSuggestionDTO> suggestForProduct(Integer productId) {
        List<ProductVariant> variants = variantRepository.findByProductIdAndIsActiveTrue(productId);
        return variants.stream()
                .map(v -> suggestForVariant(v.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<PricingSuggestionDTO> suggestAllActionable(int limit) {
        List<ProductVariant> all = variantRepository.findByIsActiveTrueOrderByIdAsc();
        double avgSalesPerDay = getAverageSalesPerDay(30);
        Map<Integer, Long> salesMap = getSalesMap(30);

        return all.stream()
                .map(v -> buildQuickSuggestion(v, salesMap, avgSalesPerDay))
                .filter(dto -> dto != null && dto.isActionable())
                .sorted(Comparator.comparingInt(d -> {
                    String a = d.getSuggestedAction();
                    if ("INCREASE_PRICE".equals(a)) return 0;
                    if ("OFFER_DISCOUNT".equals(a)) return 1;
                    return 2;
                }))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private PricingSuggestionDTO buildQuickSuggestion(ProductVariant v, Map<Integer, Long> salesMap, double avgSalesPerDay) {
        long sold = salesMap.getOrDefault(v.getId(), 0L);
        int stock = v.getSoLuongTon() != null ? v.getSoLuongTon() : 0;
        double salesPerDay = sold / 30.0;
        int daysUntilEmpty = salesPerDay > 0 ? (int) Math.round(stock / salesPerDay) : 999;

        int invScore = calcInventoryScore(daysUntilEmpty);
        int velScore = calcVelocityScore(salesPerDay, avgSalesPerDay);
        int combined = (int) Math.round(invScore * 0.5 + velScore * 0.5);

        if (combined < 35 && stock > 20) {
            PricingSuggestionDTO dto = new PricingSuggestionDTO();
            dto.setVariantId(v.getId());
            dto.setVariantName(v.getTenBienThe());
            dto.setProductId(v.getProductId());
            dto.setProductName(v.getProduct() != null ? v.getProduct().getTenSanPham() : "");
            dto.setCurrentGiaGoc(v.getGiaGoc());
            dto.setCurrentGiaKhuyenMai(v.getGiaKhuyenMai());
            dto.setCurrentStock(stock);
            dto.setSalesPerDay(Math.round(salesPerDay * 10.0) / 10.0);
            dto.setDaysUntilEmpty(daysUntilEmpty);
            dto.setSuggestedAction("OFFER_DISCOUNT");
            BigDecimal salePrice = v.getGiaGoc()
                    .multiply(new BigDecimal("0.88")).setScale(0, RoundingMode.HALF_UP);
            dto.setSuggestedGiaGoc(v.getGiaGoc());
            dto.setSuggestedGiaKhuyenMai(salePrice);
            dto.setSuggestedDiscountPct(12);
            dto.setReason("Tồn kho " + stock + ", bán chậm. Giảm giá 12% để giải phóng hàng.");
            dto.setConfidence("MEDIUM");
            dto.setActionable(true);
            return dto;
        }

        if (combined >= 65 && daysUntilEmpty < 20) {
            PricingSuggestionDTO dto = new PricingSuggestionDTO();
            dto.setVariantId(v.getId());
            dto.setVariantName(v.getTenBienThe());
            dto.setProductId(v.getProductId());
            dto.setProductName(v.getProduct() != null ? v.getProduct().getTenSanPham() : "");
            dto.setCurrentGiaGoc(v.getGiaGoc());
            dto.setCurrentGiaKhuyenMai(v.getGiaKhuyenMai());
            dto.setCurrentStock(stock);
            dto.setSalesPerDay(Math.round(salesPerDay * 10.0) / 10.0);
            dto.setDaysUntilEmpty(daysUntilEmpty);
            dto.setSuggestedAction("INCREASE_PRICE");
            BigDecimal increase = v.getGiaGoc().multiply(new BigDecimal("1.08")).setScale(0, RoundingMode.DOWN);
            dto.setSuggestedGiaGoc(increase);
            dto.setSuggestedGiaKhuyenMai(v.getGiaKhuyenMai());
            dto.setReason("Bán chạy, sắp hết hàng (" + daysUntilEmpty + " ngày). Tăng giá 8%.");
            dto.setConfidence("MEDIUM");
            dto.setActionable(true);
            return dto;
        }

        return null;
    }

    // ── Helpers ──

    private long getSoldInDays(Integer variantId, int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Object[]> raw = orderItemRepository.sumSoldByVariantInRange(start, end);
        for (Object[] row : raw) {
            if (row[0].equals(variantId)) {
                return row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            }
        }
        return 0L;
    }

    private Map<Integer, Long> getSalesMap(int days) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<Object[]> raw = orderItemRepository.sumSoldByVariantInRange(start, end);
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : raw) {
            map.put((Integer) row[0], row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
        }
        return map;
    }

    private double getAverageSalesPerDay(int days) {
        Map<Integer, Long> salesMap = getSalesMap(days);
        if (salesMap.isEmpty()) return 0;
        long totalSold = salesMap.values().stream().mapToLong(Long::longValue).sum();
        long variantCount = salesMap.size();
        if (variantCount == 0) return 0;
        return (totalSold / (double) variantCount) / days;
    }

    private int calcInventoryScore(int daysUntilEmpty) {
        if (daysUntilEmpty < 7) return 90;
        if (daysUntilEmpty < 15) return 75;
        if (daysUntilEmpty < 30) return 55;
        if (daysUntilEmpty < 60) return 35;
        if (daysUntilEmpty < 90) return 20;
        return 10;
    }

    private int calcVelocityScore(double salesPerDay, double avgSalesPerDay) {
        if (avgSalesPerDay <= 0) return 50;
        double ratio = salesPerDay / avgSalesPerDay;
        if (ratio > 3) return 90;
        if (ratio > 2) return 75;
        if (ratio > 1) return 60;
        if (ratio > 0.5) return 40;
        if (ratio > 0.2) return 25;
        return 10;
    }
}
