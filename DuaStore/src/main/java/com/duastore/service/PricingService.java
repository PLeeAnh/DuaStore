package com.duastore.service;

import com.duastore.model.FlashSale;
import com.duastore.model.ProductVariant;
import com.duastore.repository.FlashSaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PricingService {

    public enum PriceSource {
        BASE, VARIANT_SALE, FLASH_SALE
    }

    public record PriceResult(
            BigDecimal originalPrice,
            BigDecimal finalPrice,
            Integer discountPercent,
            PriceSource source,
            FlashSale flashSale
            ) {

    }

    private final FlashSaleRepository flashSaleRepository;

    public PricingService(FlashSaleRepository flashSaleRepository) {
        this.flashSaleRepository = flashSaleRepository;
    }

    public PriceResult resolvePrice(ProductVariant variant, FlashSale activeFlashSale) {
        BigDecimal giaGoc = variant.getGiaGoc();
        if (giaGoc == null) {
            giaGoc = BigDecimal.ZERO;
        }
        BigDecimal giaKM = variant.getGiaKhuyenMai();

        BigDecimal flashPrice = null;
        if (activeFlashSale != null && isFlashSaleUsable(activeFlashSale)) {
            flashPrice = giaGoc.multiply(
                    BigDecimal.ONE.subtract(
                            activeFlashSale.getGiaTriGiam().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                    )).setScale(0, RoundingMode.HALF_UP);
        }

        BigDecimal best = giaGoc;
        PriceSource src = PriceSource.BASE;

        if (giaKM != null && giaKM.compareTo(best) < 0) {
            best = giaKM;
            src = PriceSource.VARIANT_SALE;
        }
        if (flashPrice != null && flashPrice.compareTo(best) < 0) {
            best = flashPrice;
            src = PriceSource.FLASH_SALE;
        }

        int pct = giaGoc.compareTo(BigDecimal.ZERO) > 0
                ? giaGoc.subtract(best).multiply(new BigDecimal("100")).divide(giaGoc, 0, RoundingMode.DOWN).intValue()
                : 0;

        return new PriceResult(giaGoc, best, pct, src, "FLASH_SALE".equals(src.name()) ? activeFlashSale : null);
    }

    public PriceResult resolvePrice(ProductVariant variant) {
        FlashSale fs = flashSaleRepository
                .findByProductIdInAndIsActiveTrue(List.of(variant.getProductId()))
                .stream()
                .filter(this::isWithinTimeWindow)
                .findFirst()
                .orElse(null);
        return resolvePrice(variant, fs);
    }

    public Map<Integer, FlashSale> loadActiveFlashSaleMap(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        return flashSaleRepository.findByProductIdInAndIsActiveTrue(productIds).stream()
                .filter(this::isWithinTimeWindow)
                .filter(this::hasRemainingQuota)
                .collect(Collectors.toMap(FlashSale::getProductId, fs -> fs, (a, b) -> a));
    }

    private boolean isWithinTimeWindow(FlashSale fs) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(fs.getNgayBatDau()) && !now.isAfter(fs.getNgayKetThuc());
    }

    private boolean hasRemainingQuota(FlashSale fs) {
        return fs.getSoLuongDaBan() < fs.getSoLuongToiDa();
    }

    public boolean isFlashSaleUsable(FlashSale fs) {
        if (fs == null || !Boolean.TRUE.equals(fs.getIsActive())) {
            return false;
        }
        return isWithinTimeWindow(fs) && hasRemainingQuota(fs);
    }

    public boolean incrementSoldQuantity(FlashSale fs, int soLuong) {
        int current = fs.getSoLuongDaBan() == null ? 0 : fs.getSoLuongDaBan();
        int newSold = current + soLuong;
        if (newSold > fs.getSoLuongToiDa()) {
            return false;
        }
        fs.setSoLuongDaBan(newSold);
        return true;
    }

    public void decrementSoldQuantity(FlashSale fs, int soLuong) {
        int current = fs.getSoLuongDaBan() == null ? 0 : fs.getSoLuongDaBan();
        fs.setSoLuongDaBan(Math.max(0, current - soLuong));
    }
}
