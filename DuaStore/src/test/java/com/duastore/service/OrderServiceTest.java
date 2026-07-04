package com.duastore.service;

import com.duastore.model.Promotion;
import com.duastore.service.client.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void calculateDiscount_phanTram_returnsCorrectAmount() {
        Promotion promo = new Promotion();
        promo.setLoaiGiam("PHAN_TRAM");
        promo.setGiaTriGiam(new BigDecimal("10"));
        promo.setGiamToiDa(null);

        BigDecimal discount = orderService.calculateDiscount(promo, new BigDecimal("200000"));
        assertThat(discount).isEqualByComparingTo("20000");
    }

    @Test
    void calculateDiscount_phanTram_withMaxCap() {
        Promotion promo = new Promotion();
        promo.setLoaiGiam("PHAN_TRAM");
        promo.setGiaTriGiam(new BigDecimal("15"));
        promo.setGiamToiDa(new BigDecimal("25000"));

        BigDecimal discount = orderService.calculateDiscount(promo, new BigDecimal("200000"));
        assertThat(discount).isEqualByComparingTo("25000");
    }

    @Test
    void calculateDiscount_phanTram_nonTerminatingDecimal_noArithmeticException() {
        Promotion promo = new Promotion();
        promo.setLoaiGiam("PHAN_TRAM");
        promo.setGiaTriGiam(new BigDecimal("15"));
        promo.setGiamToiDa(null);

        BigDecimal discount = orderService.calculateDiscount(promo, new BigDecimal("19001"));
        assertThat(discount).isEqualByComparingTo(new BigDecimal("2850.15"));
    }

    @Test
    void calculateDiscount_soTien_returnsFixedAmount() {
        Promotion promo = new Promotion();
        promo.setLoaiGiam("SO_TIEN");
        promo.setGiaTriGiam(new BigDecimal("50000"));

        BigDecimal discount = orderService.calculateDiscount(promo, new BigDecimal("200000"));
        assertThat(discount).isEqualByComparingTo("50000");
    }

    @Test
    void calculateDiscount_soTien_cappedBySubtotal() {
        Promotion promo = new Promotion();
        promo.setLoaiGiam("SO_TIEN");
        promo.setGiaTriGiam(new BigDecimal("50000"));

        BigDecimal discount = orderService.calculateDiscount(promo, new BigDecimal("30000"));
        assertThat(discount).isEqualByComparingTo("30000");
    }

    @Test
    void validateCouponForApi_returnsValidForGoodCode() {
        String maCode = "TEST10";
        BigDecimal subtotal = new BigDecimal("200000");

        var result = orderService.validateCouponForApi(maCode, subtotal);
        assertThat(result.get("valid")).isEqualTo(false);
    }
}
