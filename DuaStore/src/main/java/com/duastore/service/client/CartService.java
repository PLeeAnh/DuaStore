package com.duastore.service.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.model.CartItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CartItemRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductVariantRepository variantRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<CartItemDTO> getItems(Integer userId) {
        return cartItemRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CartResult add(Integer userId, Integer variantId, Integer quantity) {
        if (variantId == null) {
            return CartResult.fail("Vui long chon bien the san pham");
        }
        int qty = normalizeQuantity(quantity);
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null || !variant.isActive()) {
            return CartResult.fail("Bien the san pham khong ton tai");
        }
        if (variant.getSoLuongTon() <= 0) {
            return CartResult.fail("San pham da het hang");
        }

        int finalQty = Math.min(qty, variant.getSoLuongTon());
        CartItem item = cartItemRepository.findByUserIdAndVariantId(userId, variantId).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setUserId(userId);
            item.setProductId(variant.getProductId());
            item.setVariantId(variantId);
            item.setSoLuong(finalQty);
        } else {
            item.setSoLuong(Math.min(item.getSoLuong() + finalQty, variant.getSoLuongTon()));
        }
        cartItemRepository.save(item);
        return CartResult.ok(count(userId));
    }

    @Transactional
    public CartResult updateQuantity(Integer userId, Integer itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId).orElse(null);
        if (item == null || !item.getUserId().equals(userId)) {
            return CartResult.fail("Khong tim thay san pham trong gio");
        }
        ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
        if (variant == null || !variant.isActive() || variant.getSoLuongTon() <= 0) {
            cartItemRepository.delete(item);
            return CartResult.fail("San pham da het hang hoac ngung ban");
        }
        item.setSoLuong(Math.min(normalizeQuantity(quantity), variant.getSoLuongTon()));
        cartItemRepository.save(item);
        return CartResult.ok(count(userId));
    }

    @Transactional
    public void remove(Integer userId, Integer itemId) {
        cartItemRepository.deleteByIdAndUserId(itemId, userId);
    }

    public int count(Integer userId) {
        if (userId == null) return 0;
        return cartItemRepository.countByUserId(userId);
    }

    public java.util.Optional<ProductVariant> findDefaultVariant(Integer productId) {
        if (productId == null) return java.util.Optional.empty();
        var opt = variantRepository.findByProductIdAndIsDefaultTrue(productId);
        if (opt.isPresent()) return opt;
        return variantRepository.findByProductIdAndIsActiveTrue(productId).stream().findFirst();
    }

    @Transactional
    public void removeByVariantId(Integer userId, Integer variantId) {
        cartItemRepository.findByUserIdAndVariantId(userId, variantId)
                .ifPresent(item -> cartItemRepository.delete(item));
    }

    @Transactional
    public CartResult updateQuantityByVariantId(Integer userId,
                                                Integer variantId,
                                                Integer quantity) {

        CartItem item = cartItemRepository
                .findByUserIdAndVariantId(userId, variantId)
                .orElse(null);

        if (item == null) {
            return CartResult.fail("Không tìm thấy sản phẩm trong giỏ");
        }

        ProductVariant variant =
                variantRepository.findById(variantId).orElse(null);

        if (variant == null
                || !variant.isActive()
                || variant.getSoLuongTon() <= 0) {

            cartItemRepository.delete(item);

            return CartResult.fail(
                    "Sản phẩm đã hết hàng hoặc ngừng bán"
            );
        }

        int stock = variant.getSoLuongTon();

        if (quantity > stock) {
            item.setSoLuong(stock);
            cartItemRepository.save(item);

            return new CartResult(
                    false,
                    "Chỉ còn " + stock + " sản phẩm trong kho",
                    count(userId)
            );
        }

        item.setSoLuong(quantity);

        cartItemRepository.save(item);

        return CartResult.ok(count(userId));
    }

    public BigDecimal total(List<CartItemDTO> items) {
        return items.stream()
                .map(CartItemDTO::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItemDTO toDto(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getVariant();
        BigDecimal price = variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();

        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setProductId(item.getProductId());
        dto.setVariantId(item.getVariantId());
        dto.setTenSanPham(product.getTenSanPham());
        dto.setTenBienThe(variant.getTenBienThe());
        dto.setHinhAnh(variant.getHinhAnh() != null ? variant.getHinhAnh() : product.getHinhAnhChinh());
        dto.setGiaBan(price);
        dto.setSoLuong(item.getSoLuong());
        dto.setSoLuongTon(variant.getSoLuongTon());
        dto.setThanhTien(price.multiply(BigDecimal.valueOf(item.getSoLuong())));
        dto.setNgayThem(item.getNgayThem());
        return dto;
    }

    private int normalizeQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            return 1;
        }
        return Math.min(quantity, 99);
    }

    public record CartResult(boolean success, String message, int cartCount) {
        static CartResult ok(int cartCount) {
            return new CartResult(true, "OK", cartCount);
        }

        static CartResult fail(String message) {
            return new CartResult(false, message, 0);
        }
    }
}
