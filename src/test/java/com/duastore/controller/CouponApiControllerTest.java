package com.duastore.controller;

import com.duastore.service.client.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CouponApiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OrderService orderService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void validateCoupon_success_returns200() throws Exception {
        when(orderService.validateCouponForApi(any(), any(), any()))
                .thenReturn(Map.of("valid", true, "discount", 50000, "message", "Áp dụng mã thành công"));

        String body = "{\"maCode\":\"TEST10\",\"subtotal\":500000}";
        mockMvc.perform(post("/api/coupon/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discount").value(50000));
    }

    @Test
    void validateCoupon_missingSubtotal_returns400() throws Exception {
        String body = "{\"maCode\":\"TEST10\"}";
        mockMvc.perform(post("/api/coupon/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Thiếu thông tin subtotal"));
    }

    @Test
    void validateCoupon_invalidSubtotal_returns400() throws Exception {
        String body = "{\"maCode\":\"TEST10\",\"subtotal\":\"not-a-number\"}";
        mockMvc.perform(post("/api/coupon/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Subtotal không hợp lệ"));
    }

    @Test
    void validateCoupon_nullMaCode_returns200() throws Exception {
        when(orderService.validateCouponForApi(any(), any(), any()))
                .thenReturn(Map.of("valid", false, "message", "Mã giảm giá không tồn tại"));

        String body = "{\"maCode\":null,\"subtotal\":500000}";
        mockMvc.perform(post("/api/coupon/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
