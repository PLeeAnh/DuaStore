package com.duastore.service.admin;

import com.duastore.model.ProductVariant;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductRepository;
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
public class AdminVariantPredictionService {

    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public AdminVariantPredictionService(OrderItemRepository orderItemRepository,
                                          ProductVariantRepository variantRepository,
                                          ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    public List<Map<String, Object>> getRestockRecommendations(int days, int limit) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(days);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        List<Object[]> salesRaw = orderItemRepository.sumSoldByVariantInRange(start, end);
        Map<Integer, Long> salesMap = new HashMap<>();
        for (Object[] row : salesRaw) {
            Integer variantId = (Integer) row[0];
            Long qty = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            salesMap.put(variantId, qty);
        }

        List<ProductVariant> allVariants = variantRepository.findByIsActiveTrueOrderByIdAsc();
        double periodDays = days;

        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductVariant v : allVariants) {
            Integer vid = v.getId();
            long sold = salesMap.getOrDefault(vid, 0L);
            int stock = v.getSoLuongTon() != null ? v.getSoLuongTon() : 0;
            if (sold == 0 && stock > 10) continue;

            double salesPerDay = sold / periodDays;
            String productName = "";
            if (v.getProduct() != null) {
                productName = v.getProduct().getTenSanPham();
            } else {
                productName = "SP #" + v.getProductId();
            }

            double daysUntilEmpty = salesPerDay > 0 ? stock / salesPerDay : 999;
            double restockScore = salesPerDay * 100 - stock;

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("variantId", vid);
            m.put("variantName", v.getTenBienThe());
            m.put("productName", productName);
            m.put("productId", v.getProductId());
            m.put("sold", sold);
            m.put("stock", stock);
            m.put("salesPerDay", Math.round(salesPerDay * 10) / 10.0);
            m.put("daysUntilEmpty", Math.round(daysUntilEmpty * 10) / 10.0);
            m.put("score", (double) Math.round(restockScore));
            m.put("dungTich", v.getDungTich());
            result.add(m);
        }

        result.sort((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")));
        return result.stream().limit(limit).collect(Collectors.toList());
    }
}
