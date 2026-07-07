package com.duastore.service;

import com.duastore.model.CartItem;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CartItemRepository;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        Category cat = new Category();
        cat.setTenDanhMuc("Test Category");
        cat.setActive(true);
        cat = categoryRepository.save(cat);

        product = new Product();
        product.setTenSanPham("Test Product");
        product.setTrangThaiSanPham("DANG_BAN");
        product.setActive(true);
        product.setDanhMucId(cat.getId());
        product = productRepository.save(product);

        variant = new ProductVariant();
        variant.setProductId(product.getId());
        variant.setTenBienThe("Default");
        variant.setGiaGoc(new BigDecimal("100000"));
        variant.setSoLuongTon(50);
        variant.setActive(true);
        variant.setDefault(true);
        variant = variantRepository.save(variant);
    }

    @Test
    void mergeGuestCart_createsCartItems() {
        Map<Integer, Integer> guestCart = Map.of(variant.getId(), 3);
        cartService.mergeGuestCart(999, guestCart);

        CartItem item = cartItemRepository.findByUserIdAndVariantId(999, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(3);
        assertThat(item.getProductId()).isEqualTo(product.getId());
    }

    @Test
    void mergeGuestCart_emptyCart_doesNothing() {
        cartService.mergeGuestCart(999, Map.of());
        long count = cartItemRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void mergeGuestCart_accumulatesWithExistingItem() {
        CartItem existing = new CartItem();
        existing.setUserId(999);
        existing.setProductId(product.getId());
        existing.setVariantId(variant.getId());
        existing.setSoLuong(2);
        existing.setGiaLucThem(BigDecimal.ZERO);
        cartItemRepository.save(existing);

        cartService.mergeGuestCart(999, Map.of(variant.getId(), 3));
        CartItem item = cartItemRepository.findByUserIdAndVariantId(999, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(5);
    }

    @Test
    void mergeGuestCart_nullOrEmpty_doesNothing() {
        cartService.mergeGuestCart(999, null);
        long count = cartItemRepository.count();
        assertThat(count).isZero();
    }
}
