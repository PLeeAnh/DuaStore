package com.duastore.service;

import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.FlashSaleItemRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
            FlashSaleItem flashSaleItem
            ) {

    }

    public record FlashSaleOffer(
            FlashSale event,
            BigDecimal giaSale,
            BigDecimal giaGoc,
            Integer soLuongConLai,
            Integer soLuongToiDa,
            Integer soLuongDaBan,
            Integer percentSold
    ) {
        public Integer getSoLuongConLai() {
            return soLuongConLai;
        }

        public int getPercentSold() {
            return percentSold;
        }
    }

    private final FlashSaleItemRepository flashSaleItemRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public PricingService(FlashSaleItemRepository flashSaleItemRepository,
            FlashSaleRepository flashSaleRepository,
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        this.flashSaleItemRepository = flashSaleItemRepository;
        this.flashSaleRepository = flashSaleRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public PriceResult resolvePrice(ProductVariant variant, FlashSaleItem item) {
        BigDecimal giaGoc = variant.getGiaGoc();
        if (giaGoc == null) {
            giaGoc = BigDecimal.ZERO;
        }
        BigDecimal giaKM = variant.getGiaKhuyenMai();

        BigDecimal flashPrice = null;
        if (isFlashSaleItemUsable(item)) {
            flashPrice = item.getGiaSale();
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

        return new PriceResult(giaGoc, best, pct, src, "FLASH_SALE".equals(src.name()) ? item : null);
    }

    public PriceResult resolvePrice(ProductVariant variant) {
        FlashSaleItem item = findBestActiveItemForVariant(variant.getId());
        return resolvePrice(variant, item);
    }

    public FlashSaleItem findBestActiveItemForVariant(Integer variantId) {
        if (variantId == null) {
            return null;
        }
        return flashSaleItemRepository.findBestActiveByVariantId(variantId, LocalDateTime.now())
                .filter(this::hasRemainingQuota)
                .orElse(null);
    }

    public Map<Integer, FlashSaleItem> loadActiveFlashSaleItemMap(Collection<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }
        return flashSaleItemRepository
                .findActiveByVariantIds(new ArrayList<>(variantIds), LocalDateTime.now())
                .stream()
                .filter(this::hasRemainingQuota)
                .collect(Collectors.toMap(FlashSaleItem::getVariantId, i -> i, (a, b) -> a));
    }

    public Map<Integer, FlashSaleOffer> loadActiveFlashSaleOffers(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<ProductVariant> variants = productVariantRepository.findByProductIdInAndIsActiveTrue(productIds);
        Map<Integer, FlashSaleItem> itemMap = loadActiveFlashSaleItemMap(
                variants.stream().map(ProductVariant::getId).collect(Collectors.toList()));

        Map<Integer, FlashSaleItem> bestByProduct = new HashMap<>();
        for (ProductVariant v : variants) {
            FlashSaleItem item = itemMap.get(v.getId());
            if (item == null) {
                continue;
            }
            FlashSaleItem existing = bestByProduct.get(v.getProductId());
            if (existing == null || item.getGiaSale().compareTo(existing.getGiaSale()) < 0) {
                bestByProduct.put(v.getProductId(), item);
            }
        }

        Map<Integer, FlashSaleOffer> offers = new HashMap<>();
        for (Map.Entry<Integer, FlashSaleItem> e : bestByProduct.entrySet()) {
            FlashSale event = e.getValue().getFlashSale();
            if (event != null) {
                FlashSaleItem item = e.getValue();
                offers.put(e.getKey(), new FlashSaleOffer(
                        event,
                        item.getGiaSale(),
                        item.getGiaGoc(),
                        item.getSoLuongConLai(),
                        item.getSoLuongToiDa(),
                        item.getSoLuongDaBan(),
                        item.getPercentSold()
                ));
            }
        }
        return offers;
    }

    public FlashSaleOffer loadBestOfferForProduct(Integer productId) {
        Map<Integer, FlashSaleOffer> map = loadActiveFlashSaleOffers(List.of(productId));
        return map.get(productId);
    }

    private boolean isWithinTimeWindow(FlashSale event) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(event.getNgayBatDau()) && !now.isAfter(event.getNgayKetThuc());
    }

    public boolean hasRemainingQuota(FlashSaleItem item) {
        if (item == null || item.getSoLuongToiDa() == null) {
            return false;
        }
        // Check item isActive
        if (!Boolean.TRUE.equals(item.getIsActive())) {
            return false;
        }
        int daBan = item.getSoLuongDaBan() == null ? 0 : item.getSoLuongDaBan();
        
        // Check item-level quota
        if (daBan >= item.getSoLuongToiDa()) {
            return false;
        }
        
        // Check event-level quota (calculated from items)
        FlashSale event = item.getFlashSale();
        if (event != null && event.getItems() != null) {
            int eventDaBan = event.getItems().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                    .mapToInt(i -> i.getSoLuongDaBan() == null ? 0 : i.getSoLuongDaBan())
                    .sum();
            
            Integer eventMax = event.getItems().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                    .mapToInt(i -> i.getSoLuongToiDa() == null ? 0 : i.getSoLuongToiDa())
                    .sum();
            
            if (eventMax != null && eventMax > 0 && eventDaBan >= eventMax) {
                return false;
            }
        }
        
        return true;
    }

    public boolean isFlashSaleItemUsable(FlashSaleItem item) {
        if (item == null || !Boolean.TRUE.equals(item.getIsActive())) {
            return false;
        }
        FlashSale event = item.getFlashSale();
        if (event == null || !Boolean.TRUE.equals(event.getIsActive())) {
            return false;
        }
        return isWithinTimeWindow(event) && hasRemainingQuota(item);
    }

    @Transactional
    public boolean incrementSoldQuantity(FlashSaleItem item, int soLuong) {
        if (item == null) {
            return false;
        }
        // Check item isActive
        if (!Boolean.TRUE.equals(item.getIsActive())) {
            return false;
        }
        int current = item.getSoLuongDaBan() == null ? 0 : item.getSoLuongDaBan();
        int newSold = current + soLuong;
        if (newSold > item.getSoLuongToiDa()) {
            return false;
        }
        
        // Check event-level quota (calculated from items) - need to lock FlashSale parent
        FlashSale event = item.getFlashSale();
        if (event != null && event.getItems() != null) {
            // Lock FlashSale parent to prevent race condition
            FlashSale lockedEvent = flashSaleRepository.findByIdWithLock(event.getId()).orElse(null);
            if (lockedEvent == null) {
                return false;
            }
            int eventMax = lockedEvent.getItems().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                    .mapToInt(i -> i.getSoLuongToiDa() == null ? 0 : i.getSoLuongToiDa())
                    .sum();
            
            int eventDaBan = lockedEvent.getItems().stream()
                    .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                    .mapToInt(i -> i.getSoLuongDaBan() == null ? 0 : i.getSoLuongDaBan())
                    .sum();
            
            if (eventMax > 0 && eventDaBan + soLuong > eventMax) {
                return false;
            }
        }
        
        item.setSoLuongDaBan(newSold);
        return true;
    }

    public void decrementSoldQuantity(FlashSaleItem item, int soLuong) {
        if (item == null) {
            return;
        }
        int current = item.getSoLuongDaBan() == null ? 0 : item.getSoLuongDaBan();
        item.setSoLuongDaBan(Math.max(0, current - soLuong));
    }

    @Transactional
    public void recalculateMinPrice(Integer productId) {
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(productId);
        BigDecimal min = variants.stream()
                .filter(v -> v.getGiaGoc() != null)
                .map(v -> {
                    BigDecimal p = v.getGiaKhuyenMai();
                    return (p != null && p.compareTo(v.getGiaGoc()) < 0) ? p : v.getGiaGoc();
                })
                .min(java.util.Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        productRepository.findById(productId).ifPresent(p -> {
            p.setMinPrice(min);
            productRepository.save(p);
        });
    }

    // ===== Methods for AdminFlashSaleController =====

    public String getEventStatus(FlashSale event) {
        if (event == null) {
            return "DA_KET_THUC";
        }
        if (!Boolean.TRUE.equals(event.getIsActive())) {
            return "TAM_DUNG";
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getNgayBatDau())) {
            return "SAP_DIEN_RA";
        }
        if (now.isAfter(event.getNgayKetThuc())) {
            return "DA_KET_THUC";
        }
        boolean hasItems = event.getItems() != null && !event.getItems().isEmpty();
        boolean allSoldOut = hasItems && event.getItems().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsActive()))
                .allMatch(this::isItemSoldOut);
        if (allSoldOut) {
            return "HET_HANG";
        }
        return "DANG_DIEN_RA";
    }

    public long sumRevenue(FlashSale event) {
        if (event == null || event.getItems() == null) {
            return 0L;
        }
        return event.getItems().stream()
                .filter(i -> i.getSoLuongDaBan() != null)
                .mapToLong(i -> i.getGiaSale()
                        .multiply(BigDecimal.valueOf(i.getSoLuongDaBan()))
                        .longValue())
                .sum();
    }

    public int sumSold(FlashSale event) {
        if (event == null || event.getItems() == null) {
            return 0;
        }
        return event.getItems().stream()
                .mapToInt(i -> i.getSoLuongDaBan() == null ? 0 : i.getSoLuongDaBan())
                .sum();
    }

    private boolean isItemSoldOut(FlashSaleItem item) {
        int daBan = item.getSoLuongDaBan() == null ? 0 : item.getSoLuongDaBan();
        return item.getSoLuongToiDa() == null || daBan >= item.getSoLuongToiDa();
    }
}