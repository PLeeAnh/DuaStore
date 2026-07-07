package com.duastore.service;

import com.duastore.model.FlashSale;
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

        FlashSale fs = new FlashSale();
        fs.setIsActive(true);
        fs.setGiaTriGiam(new BigDecimal("30")); // 30% off -> 70000
        fs.setNgayBatDau(LocalDateTime.now().minusDays(1));
        fs.setNgayKetThuc(LocalDateTime.now().plusDays(1));
        fs.setSoLuongDaBan(0);
        fs.setSoLuongToiDa(100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, fs);
        assertThat(r.finalPrice()).isEqualByComparingTo("70000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.FLASH_SALE);
    }

    @Test
    void resolvePrice_flashSaleEqualsVariantSale_keepsVariantSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("80000"));

        FlashSale fs = new FlashSale();
        fs.setIsActive(true);
        fs.setGiaTriGiam(new BigDecimal("20")); // 20% off -> 80000 (equals variant sale)
        fs.setNgayBatDau(LocalDateTime.now().minusDays(1));
        fs.setNgayKetThuc(LocalDateTime.now().plusDays(1));
        fs.setSoLuongDaBan(0);
        fs.setSoLuongToiDa(100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, fs);
        assertThat(r.finalPrice()).isEqualByComparingTo("80000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.VARIANT_SALE);
    }

    @Test
    void resolvePrice_flashSaleSoldOut_ignoresFlashSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));
        v.setGiaKhuyenMai(new BigDecimal("90000"));

        FlashSale fs = new FlashSale();
        fs.setIsActive(true);
        fs.setGiaTriGiam(new BigDecimal("50")); // 50% off -> 50000
        fs.setNgayBatDau(LocalDateTime.now().minusDays(1));
        fs.setNgayKetThuc(LocalDateTime.now().plusDays(1));
        fs.setSoLuongDaBan(100);
        fs.setSoLuongToiDa(100); // hết suất

        PricingService.PriceResult r = pricingService.resolvePrice(v, fs);
        assertThat(r.finalPrice()).isEqualByComparingTo("90000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.VARIANT_SALE);
    }

    @Test
    void resolvePrice_flashSaleOutOfTimeWindow_ignoresFlashSale() {
        ProductVariant v = new ProductVariant();
        v.setGiaGoc(new BigDecimal("100000"));

        FlashSale fs = new FlashSale();
        fs.setIsActive(true);
        fs.setGiaTriGiam(new BigDecimal("50"));
        fs.setNgayBatDau(LocalDateTime.now().plusDays(1)); // chưa bắt đầu
        fs.setNgayKetThuc(LocalDateTime.now().plusDays(2));
        fs.setSoLuongDaBan(0);
        fs.setSoLuongToiDa(100);

        PricingService.PriceResult r = pricingService.resolvePrice(v, fs);
        assertThat(r.finalPrice()).isEqualByComparingTo("100000");
        assertThat(r.source()).isEqualTo(PricingService.PriceSource.BASE);
    }

    @Test
    void incrementSoldQuantity_exceeds_returnsFalse() {
        FlashSale fs = new FlashSale();
        fs.setSoLuongDaBan(95);
        fs.setSoLuongToiDa(100);

        boolean ok = pricingService.incrementSoldQuantity(fs, 10);
        assertThat(ok).isFalse();
        assertThat(fs.getSoLuongDaBan()).isEqualTo(95); // unchanged
    }

    @Test
    void decrementSoldQuantity_neverNegative() {
        FlashSale fs = new FlashSale();
        fs.setSoLuongDaBan(0);

        pricingService.decrementSoldQuantity(fs, 5);
        assertThat(fs.getSoLuongDaBan()).isZero();
    }
}
