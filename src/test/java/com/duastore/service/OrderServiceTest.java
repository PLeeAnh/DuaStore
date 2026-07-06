package com.duastore.service;

import com.duastore.model.OrderItem;
import com.duastore.model.Product;
import com.duastore.model.Promotion;
import com.duastore.service.client.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    void resolveEligibleAmount_targetAll_returnsFullAmount() {
        Promotion promo = new Promotion();
        promo.setTargetType("");
        promo.setStackable(true);

        OrderItem item1 = new OrderItem();
        item1.setProductId(1);
        item1.setThanhTien(new BigDecimal("100000"));
        item1.setLoaiGia("BASE");

        OrderItem item2 = new OrderItem();
        item2.setProductId(2);
        item2.setThanhTien(new BigDecimal("50000"));
        item2.setLoaiGia("FLASH_SALE");

        BigDecimal eligible = orderService.resolveEligibleAmount(promo, List.of(item1, item2), Map.of());
        assertThat(eligible).isEqualByComparingTo("150000");
    }

    @Test
    void resolveEligibleAmount_stackableFalse_excludesFlashSaleItems() {
        Promotion promo = new Promotion();
        promo.setTargetType("");
        promo.setStackable(false);

        OrderItem normal = new OrderItem();
        normal.setProductId(1);
        normal.setThanhTien(new BigDecimal("100000"));
        normal.setLoaiGia("BASE");

        OrderItem flash = new OrderItem();
        flash.setProductId(2);
        flash.setThanhTien(new BigDecimal("50000"));
        flash.setLoaiGia("FLASH_SALE");

        BigDecimal eligible = orderService.resolveEligibleAmount(promo, List.of(normal, flash), Map.of());
        assertThat(eligible).isEqualByComparingTo("100000");
    }

    @Test
    void resolveEligibleAmount_targetCategory_filtersCorrectly() {
        Promotion promo = new Promotion();
        promo.setTargetType("CATEGORY");
        promo.setTargetIds("10");
        promo.setStackable(true);

        Product catProduct = new Product();
        catProduct.setId(1);
        catProduct.setDanhMucId(10);

        Product otherProduct = new Product();
        otherProduct.setId(2);
        otherProduct.setDanhMucId(20);

        Map<Integer, Product> productById = new HashMap<>();
        productById.put(1, catProduct);
        productById.put(2, otherProduct);

        OrderItem item1 = new OrderItem();
        item1.setProductId(1);
        item1.setThanhTien(new BigDecimal("100000"));
        item1.setLoaiGia("BASE");

        OrderItem item2 = new OrderItem();
        item2.setProductId(2);
        item2.setThanhTien(new BigDecimal("50000"));
        item2.setLoaiGia("BASE");

        BigDecimal eligible = orderService.resolveEligibleAmount(promo, List.of(item1, item2), productById);
        assertThat(eligible).isEqualByComparingTo("100000");
    }

    @Test
    void resolveEligibleAmount_targetProduct_filtersCorrectly() {
        Promotion promo = new Promotion();
        promo.setTargetType("PRODUCT");
        promo.setTargetIds("5");
        promo.setStackable(true);

        OrderItem match = new OrderItem();
        match.setProductId(5);
        match.setThanhTien(new BigDecimal("200000"));
        match.setLoaiGia("BASE");

        OrderItem noMatch = new OrderItem();
        noMatch.setProductId(99);
        noMatch.setThanhTien(new BigDecimal("100000"));
        noMatch.setLoaiGia("BASE");

        BigDecimal eligible = orderService.resolveEligibleAmount(promo, List.of(match, noMatch), Map.of());
        assertThat(eligible).isEqualByComparingTo("200000");
    }

    @Test
    void resolveEligibleAmount_stackableFalseWithTarget_combinesCorrectly() {
        Promotion promo = new Promotion();
        promo.setTargetType("CATEGORY");
        promo.setTargetIds("10");
        promo.setStackable(false);

        Product catProduct = new Product();
        catProduct.setId(1);
        catProduct.setDanhMucId(10);

        Map<Integer, Product> productById = new HashMap<>();
        productById.put(1, catProduct);
        productById.put(2, null);

        OrderItem normalCat = new OrderItem();
        normalCat.setProductId(1);
        normalCat.setThanhTien(new BigDecimal("100000"));
        normalCat.setLoaiGia("BASE");

        OrderItem flashCat = new OrderItem();
        flashCat.setProductId(1);
        flashCat.setThanhTien(new BigDecimal("50000"));
        flashCat.setLoaiGia("FLASH_SALE");

        OrderItem other = new OrderItem();
        other.setProductId(2);
        other.setThanhTien(new BigDecimal("100000"));
        other.setLoaiGia("BASE");

        BigDecimal eligible = orderService.resolveEligibleAmount(promo, List.of(normalCat, flashCat, other), productById);
        // only normalCat matches (flashCat excluded by stackable=false, other excluded by category)
        assertThat(eligible).isEqualByComparingTo("100000");
    }
}
