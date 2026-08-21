package com.duastore.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PriceUtilsTest {

    @Test
    void format_returnsFormattedPrice() {
        assertThat(PriceUtils.format(new BigDecimal("100000"))).isEqualTo("100.000₫");
    }

    @Test
    void format_largeNumber() {
        assertThat(PriceUtils.format(new BigDecimal("2280469"))).isEqualTo("2.280.469₫");
    }

    @Test
    void format_zero() {
        assertThat(PriceUtils.format(BigDecimal.ZERO)).isEqualTo("0₫");
    }

    @Test
    void format_null_returnsZero() {
        assertThat(PriceUtils.format(null)).isEqualTo("0₫");
    }

    @Test
    void format_smallNumber_lessThan1000() {
        assertThat(PriceUtils.format(new BigDecimal("500"))).isEqualTo("500₫");
    }

    @Test
    void format_thousands() {
        assertThat(PriceUtils.format(new BigDecimal("999999"))).isEqualTo("999.999₫");
    }

    @Test
    void format_millions() {
        assertThat(PriceUtils.format(new BigDecimal("1000000"))).isEqualTo("1.000.000₫");
    }

    @Test
    void format_decimalValues_ignoresDecimals() {
        assertThat(PriceUtils.format(new BigDecimal("1234.56"))).isEqualTo("1.235₫");
    }
}
