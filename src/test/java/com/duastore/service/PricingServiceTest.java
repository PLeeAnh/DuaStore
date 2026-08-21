package com.duastore.service;

import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.ProductVariant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PricingServiceTest {

    @Autowired
    private PricingService pricingService;

    private FlashSaleItem activeItem(BigDecimal giaSale, int daBan, int toiDa) {
        return activeItem(giaSale, daBan, toiDa,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    }

    private FlashSaleItem activeItem(BigDecimal giaSale, int daBan, int toiDa,
            LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        FlashSale fs = new FlashSale();
        fs.setIsActive(true);
        fs.setNgayBatDau(ngayBatDau);
        fs.setNgayKetThuc(ngayKetThuc);

        FlashSaleItem item = new FlashSaleItem();
        fs.addItem(item);
        item.setIsActive(true);
        item.setGiaSale(giaSale);
        item.setSoLuongDaBan(daBan);
        item.setSoLuongToiDa(toiDa);
        return item;
    }

    @Test
    void resolvePrice_noFlashNoSale_returnsBasePrice() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));

        PricingService.PriceResult r = pricingService.resolvePrice(v, null);
        assertThat(r.finalPrice()).isEqualByComparingTo("100000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.BASE);
    }

    @Test
    void resolvePrice_variantSale_lowerThanBase_returnsVariantSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("80000"));

        PricingService.PriceResult r = pricingService.resolvePrice(v, null);
        assertThat(r.finalPrice()).isEqualByComparingTo("80000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.VARIANT_SALE);
    }

    @Test
    void resolvePrice_flashSale_lowerThanVariantSale_returnsFlashSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("80000"));

        FlashSaleItem item = activeItem(new BigDecimal("70000"), 0, 100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, item);
        assertThat(r.finalPrice()).isEqualByComparingTo("70000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.FLASH_SALE);
    }

    @Test
    void resolvePrice_flashSaleEqualsVariantSale_keepsVariantSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("80000"));

        FlashSaleItem item = activeItem(new BigDecimal("80000"), 0, 100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, item);
        assertThat(r.finalPrice()).isEqualByComparingTo("80000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.VARIANT_SALE);
    }

    @Test
    void resolvePrice_flashSaleSoldOut_ignoresFlashSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("90000"));

        FlashSaleItem item = activeItem(new BigDecimal("50000"), 100, 100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, item);
        assertThat(r.finalPrice()).isEqualByComparingTo("90000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.VARIANT_SALE);
    }

    @Test
    void resolvePrice_flashSaleOutOfTimeWindow_ignoresFlashSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));

        FlashSaleItem item = activeItem(new BigDecimal("50000"), 0, 100,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        PricingService.PriceResult r = pricingService.resolvePrice(v, item);
        assertThat(r.finalPrice()).isEqualByComparingTo("100000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.BASE);
    }

    @Test
    void incrementSoldQuantity_exceeds_returnsFalse() {
        FlashSaleItem item = new FlashSaleItem();
        item.setSoLuongDaBan(95);
        item.setSoLuongToiDa(100);

        boolean ok = pricingService.incrementSoldQuantity(item, 10);
        assertThat(ok).isFalse();
        assertThat(item.getSoLuongDaBan()).isEqualTo(95); // unchanged
    }

    @Test
    void decrementSoldQuantity_neverNegative() {
        FlashSaleItem item = new FlashSaleItem();
        item.setSoLuongDaBan(0);

        pricingService.decrementSoldQuantity(item, 5);
        assertThat(item.getSoLuongDaBan()).isZero();
    }
}