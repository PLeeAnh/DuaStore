package com.duastore.controller;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CartWishlistApiControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CartService cartService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        CartService cartService() {
            return Mockito.mock(CartService.class);
        }

        @Bean
        @Primary
        WishlistService wishlistService() {
            return Mockito.mock(WishlistService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.deleteAll();

        User user = new User();
        user.setUsername("api-test-user");
        user.setEmail("api-test@example.com");
        user.setPassword("pass");
        user.setHoTen("API Test User");
        user.setIsActive(true);
        user.setNgayTao(LocalDateTime.now());
        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void toggleWishlist_success_returns200() throws Exception {
        when(wishlistService.toggle(anyInt(), anyInt())).thenReturn(true);

        String body = "{\"productId\":1}";
        mockMvc.perform(post("/api/wishlist/toggle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.isLiked").value(true));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void toggleWishlist_missingProductId_returns400() throws Exception {
        String body = "{}";
        mockMvc.perform(post("/api/wishlist/toggle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Thiếu thông tin sản phẩm"));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void addToCart_withVariantId_returns200() throws Exception {
        when(cartService.add(anyInt(), anyInt(), anyInt()))
                .thenReturn(new CartService.CartResult(true, "OK", 3));

        String body = "{\"productId\":1,\"variantId\":10,\"quantity\":2}";
        mockMvc.perform(post("/api/cart/add-popup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartCount").value(3));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void addToCart_cartServiceFails_returns400() throws Exception {
        when(cartService.add(anyInt(), anyInt(), anyInt()))
                .thenReturn(new CartService.CartResult(false, "San pham da het hang", 0));

        String body = "{\"productId\":1,\"variantId\":10,\"quantity\":2}";
        mockMvc.perform(post("/api/cart/add-popup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("San pham da het hang"));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void removeCartItem_success_returns200() throws Exception {
        doNothing().when(cartService).removeByVariantId(anyInt(), anyInt());
        when(cartService.count(anyInt())).thenReturn(2);

        String body = "{\"variantId\":10}";
        mockMvc.perform(post("/api/cart/remove-item")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartCount").value(2));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void removeCartItem_missingVariantId_returns400() throws Exception {
        String body = "{}";
        mockMvc.perform(post("/api/cart/remove-item")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Thiếu thông tin biến thể"));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void updateCartItem_success_returns200() throws Exception {
        when(cartService.updateQuantityByVariantId(anyInt(), anyInt(), anyInt()))
                .thenReturn(new CartService.CartResult(true, "OK", 3));

        String body = "{\"variantId\":10,\"soLuong\":3}";
        mockMvc.perform(post("/api/cart/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.cartCount").value(3));
    }

    @Test
    @WithMockUser(username = "api-test@example.com")
    void updateCartItem_invalidQuantity_returns400() throws Exception {
        String body = "{\"variantId\":10,\"soLuong\":0}";
        mockMvc.perform(post("/api/cart/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Số lượng không hợp lệ"));
    }
}
