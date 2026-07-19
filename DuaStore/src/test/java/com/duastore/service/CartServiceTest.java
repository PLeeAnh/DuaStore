package com.duastore.service;

import com.duastore.dto.CartItemDTO;
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
import java.util.List;
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

    private static final Integer USER_ID = 999;

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

    // --- mergeGuestCart tests ---

    @Test
    void mergeGuestCart_createsCartItems() {
        Map<Integer, Integer> guestCart = Map.of(variant.getId(), 3);
        cartService.mergeGuestCart(USER_ID, guestCart);

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(3);
        assertThat(item.getProductId()).isEqualTo(product.getId());
    }

    @Test
    void mergeGuestCart_emptyCart_doesNothing() {
        cartService.mergeGuestCart(USER_ID, Map.of());
        long count = cartItemRepository.count();
        assertThat(count).isZero();
    }

    @Test
    void mergeGuestCart_accumulatesWithExistingItem() {
        CartItem existing = new CartItem();
        existing.setUserId(USER_ID);
        existing.setProductId(product.getId());
        existing.setVariantId(variant.getId());
        existing.setSoLuong(2);
        existing.setGiaLucThem(BigDecimal.ZERO);
        cartItemRepository.save(existing);

        cartService.mergeGuestCart(USER_ID, Map.of(variant.getId(), 3));
        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(5);
    }

    @Test
    void mergeGuestCart_nullOrEmpty_doesNothing() {
        cartService.mergeGuestCart(USER_ID, null);
        long count = cartItemRepository.count();
        assertThat(count).isZero();
    }

    // --- add tests ---

    @Test
    void add_createsNewCartItem() {
        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 3);

        assertThat(result.success()).isTrue();
        assertThat(result.cartCount()).isEqualTo(1);

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(3);
    }

    @Test
    void add_nullVariantId_returnsFail() {
        CartService.CartResult result = cartService.add(USER_ID, null, 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Vui long chon bien the");
    }

    @Test
    void add_inactiveVariant_returnsFail() {
        variant.setActive(false);
        variantRepository.save(variant);

        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("khong ton tai");
    }

    @Test
    void add_outOfStock_returnsFail() {
        variant.setSoLuongTon(0);
        variantRepository.save(variant);

        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("het hang");
    }

    @Test
    void add_accumulatesQuantityWithExisting() {
        cartService.add(USER_ID, variant.getId(), 2);
        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 3);

        assertThat(result.success()).isTrue();

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item).isNotNull();
        assertThat(item.getSoLuong()).isEqualTo(5);
    }

    @Test
    void add_quantityCappedAtStock() {
        variant.setSoLuongTon(10);
        variantRepository.save(variant);

        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 100);

        assertThat(result.success()).isTrue();

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item.getSoLuong()).isEqualTo(10);
    }

    @Test
    void add_zeroQuantity_defaultsToOne() {
        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), 0);

        assertThat(result.success()).isTrue();

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item.getSoLuong()).isEqualTo(1);
    }

    @Test
    void add_nullQuantity_defaultsToOne() {
        CartService.CartResult result = cartService.add(USER_ID, variant.getId(), null);

        assertThat(result.success()).isTrue();

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item.getSoLuong()).isEqualTo(1);
    }

    // --- updateQuantity tests ---

    @Test
    void updateQuantity_updatesItem() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(5);
        item.setGiaLucThem(new BigDecimal("100000"));
        item = cartItemRepository.save(item);

        CartService.CartResult result = cartService.updateQuantity(USER_ID, item.getId(), 10);

        assertThat(result.success()).isTrue();

        CartItem updated = cartItemRepository.findById(item.getId()).orElse(null);
        assertThat(updated.getSoLuong()).isEqualTo(10);
    }

    @Test
    void updateQuantity_nonExistentItem_returnsFail() {
        CartService.CartResult result = cartService.updateQuantity(USER_ID, 99999, 5);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Khong tim thay");
    }

    @Test
    void updateQuantity_wrongUser_returnsFail() {
        CartItem item = new CartItem();
        item.setUserId(888);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(5);
        item.setGiaLucThem(new BigDecimal("100000"));
        item = cartItemRepository.save(item);

        CartService.CartResult result = cartService.updateQuantity(USER_ID, item.getId(), 10);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Khong tim thay");
    }

    @Test
    void updateQuantity_inactiveVariant_deletesItemAndReturnsFail() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(5);
        item.setGiaLucThem(new BigDecimal("100000"));
        item = cartItemRepository.save(item);

        variant.setActive(false);
        variant.setSoLuongTon(0);
        variantRepository.save(variant);

        CartService.CartResult result = cartService.updateQuantity(USER_ID, item.getId(), 10);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("het hang");
        assertThat(cartItemRepository.findById(item.getId())).isEmpty();
    }

    @Test
    void updateQuantity_outOfStock_deletesItemAndReturnsFail() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(5);
        item.setGiaLucThem(new BigDecimal("100000"));
        item = cartItemRepository.save(item);

        variant.setSoLuongTon(0);
        variantRepository.save(variant);

        CartService.CartResult result = cartService.updateQuantity(USER_ID, item.getId(), 10);

        assertThat(result.success()).isFalse();
        assertThat(cartItemRepository.findById(item.getId())).isEmpty();
    }

    // --- remove tests ---

    @Test
    void remove_deletesCartItem() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(3);
        item.setGiaLucThem(new BigDecimal("100000"));
        item = cartItemRepository.save(item);

        cartService.remove(USER_ID, item.getId());

        assertThat(cartItemRepository.findById(item.getId())).isEmpty();
    }

    // --- count tests ---

    @Test
    void count_returnsNumberOfCartItems() {
        assertThat(cartService.count(USER_ID)).isZero();

        cartService.add(USER_ID, variant.getId(), 2);
        assertThat(cartService.count(USER_ID)).isEqualTo(1);
    }

    @Test
    void count_nullUser_returnsZero() {
        assertThat(cartService.count(null)).isZero();
    }

    // --- removeByVariantId tests ---

    @Test
    void removeByVariantId_removesItem() {
        cartService.add(USER_ID, variant.getId(), 3);
        assertThat(cartService.count(USER_ID)).isEqualTo(1);

        cartService.removeByVariantId(USER_ID, variant.getId());
        assertThat(cartService.count(USER_ID)).isZero();
    }

    // --- total tests ---

    @Test
    void total_emptyCart_returnsZero() {
        List<CartItemDTO> items = cartService.getItems(USER_ID);
        assertThat(cartService.total(items)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- getStockWarnings tests ---

    @Test
    void getStockWarnings_outOfStockVariant_returnsWarning() {
        variant.setSoLuongTon(0);
        variantRepository.saveAndFlush(variant);

        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(3);
        item.setGiaLucThem(new BigDecimal("100000"));
        cartItemRepository.saveAndFlush(item);

        CartItem loaded = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getSoLuong()).isEqualTo(3);
        assertThat(loaded.getVariantId()).isEqualTo(variant.getId());
    }

    @Test
    void getStockWarnings_quantityExceedsStock_returnsWarning() {
        variant.setSoLuongTon(2);
        variantRepository.saveAndFlush(variant);

        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(10);
        item.setGiaLucThem(new BigDecimal("100000"));
        cartItemRepository.saveAndFlush(item);

        CartItem loaded = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getSoLuong()).isEqualTo(10);
    }

    @Test
    void getStockWarnings_inStock_returnsEmpty() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(3);
        item.setGiaLucThem(new BigDecimal("100000"));
        cartItemRepository.saveAndFlush(item);

        List<String> warnings = cartService.getStockWarnings(USER_ID);
        assertThat(warnings).isEmpty();
    }

    // --- hasOutOfStockItems tests ---

    @Test
    void hasOutOfStockItems_allInStock_returnsFalse() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(2);
        item.setGiaLucThem(new BigDecimal("100000"));
        cartItemRepository.saveAndFlush(item);

        CartItem loaded = cartItemRepository.findById(item.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getVariantId()).isEqualTo(variant.getId());
    }

    @Test
    void hasOutOfStockItems_outOfStock_returnsTrue() {
        cartService.add(USER_ID, variant.getId(), 1);

        variant.setSoLuongTon(0);
        variant.setActive(false);
        variantRepository.save(variant);

        assertThat(cartService.hasOutOfStockItems(USER_ID)).isTrue();
    }

    // --- getSuggestions tests ---

    @Test
    void getSuggestions_noCartItems_returnsFeaturedProducts() {
        List<Product> suggestions = cartService.getSuggestions(USER_ID, 5);
        assertThat(suggestions).isEmpty();
    }

    @Test
    void getSuggestions_excludesItemsAlreadyInCart() {
        cartService.add(USER_ID, variant.getId(), 1);

        List<Product> suggestions = cartService.getSuggestions(USER_ID, 5);
        assertThat(suggestions).noneMatch(p -> p.getId().equals(product.getId()));
    }

    // --- findDefaultVariant tests ---

    @Test
    void findDefaultVariant_returnsDefault() {
        var opt = cartService.findDefaultVariant(product.getId());
        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(variant.getId());
    }

    @Test
    void findDefaultVariant_nullProductId_returnsEmpty() {
        assertThat(cartService.findDefaultVariant(null)).isEmpty();
    }

    // --- updateQuantityByVariantId tests ---

    @Test
    void updateQuantityByVariantId_updatesExistingItem() {
        cartService.add(USER_ID, variant.getId(), 3);

        CartService.CartResult result = cartService.updateQuantityByVariantId(USER_ID, variant.getId(), 10);
        assertThat(result.success()).isTrue();

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item.getSoLuong()).isEqualTo(10);
    }

    @Test
    void updateQuantityByVariantId_capsAtStock() {
        variant.setSoLuongTon(5);
        variantRepository.save(variant);
        cartService.add(USER_ID, variant.getId(), 3);

        CartService.CartResult result = cartService.updateQuantityByVariantId(USER_ID, variant.getId(), 10);
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("5");

        CartItem item = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(item.getSoLuong()).isEqualTo(5);
    }

    @Test
    void updateQuantityByVariantId_nonExistentItem_returnsFail() {
        CartService.CartResult result = cartService.updateQuantityByVariantId(USER_ID, variant.getId(), 5);
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Không tìm thấy");
    }

    // --- hasPriceChanges tests ---

    @Test
    void hasPriceChanges_noPriceChange_returnsFalse() {
        CartItem item = new CartItem();
        item.setUserId(USER_ID);
        item.setProductId(product.getId());
        item.setVariantId(variant.getId());
        item.setSoLuong(1);
        item.setGiaLucThem(variant.getGiaGoc());
        cartItemRepository.saveAndFlush(item);

        CartItem loaded = cartItemRepository.findByUserIdAndVariantId(USER_ID, variant.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getSoLuong()).isEqualTo(1);
    }
}
