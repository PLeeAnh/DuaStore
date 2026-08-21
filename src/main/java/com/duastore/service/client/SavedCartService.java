package com.duastore.service.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.model.CartItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.SavedCartItem;
import com.duastore.repository.CartItemRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.SavedCartItemRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý giỏ hàng đã lưu, giỏ hàng.
 */
public class SavedCartService {

    private final SavedCartItemRepository savedRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;

    public SavedCartService(SavedCartItemRepository savedRepository,
            CartItemRepository cartItemRepository,
            ProductVariantRepository variantRepository) {
        this.savedRepository = savedRepository;
        this.cartItemRepository = cartItemRepository;
        this.variantRepository = variantRepository;
    }

    public List<CartItemDTO> getSavedItems(Integer userId) {
        return savedRepository.findByUserIdOrderByNgayLuuDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public boolean saveForLater(Integer userId, Integer variantId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndVariantId(userId, variantId).orElse(null);
        if (cartItem == null) {
            return false;
        }

        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return false;
        }

        SavedCartItem existing = savedRepository.findByUserIdAndVariantId(userId, variantId).orElse(null);
        if (existing != null) {
            existing.setSoLuong(existing.getSoLuong() + cartItem.getSoLuong());
            BigDecimal price = variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();
            existing.setGiaLuu(price);
            savedRepository.save(existing);
        } else {
            SavedCartItem saved = new SavedCartItem();
            saved.setUserId(userId);
            saved.setProductId(cartItem.getProductId());
            saved.setVariantId(variantId);
            saved.setSoLuong(cartItem.getSoLuong());
            BigDecimal price = variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();
            saved.setGiaLuu(price);
            savedRepository.save(saved);
        }
        cartItemRepository.deleteByIdAndUserId(cartItem.getId(), userId);
        return true;
    }

    @Transactional
    public boolean moveToCart(Integer userId, Integer savedId) {
        SavedCartItem saved = savedRepository.findById(savedId).orElse(null);
        if (saved == null || !saved.getUserId().equals(userId)) {
            return false;
        }

        ProductVariant variant = variantRepository.findById(saved.getVariantId()).orElse(null);
        if (variant == null || !variant.isActive() || variant.getSoLuongTon() <= 0) {
            return false;
        }

        int finalQty = Math.min(saved.getSoLuong(), variant.getSoLuongTon());
        CartItem existing = cartItemRepository.findByUserIdAndVariantId(userId, saved.getVariantId()).orElse(null);
        if (existing != null) {
            existing.setSoLuong(Math.min(existing.getSoLuong() + finalQty, variant.getSoLuongTon()));
            cartItemRepository.save(existing);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(saved.getProductId());
            cartItem.setVariantId(saved.getVariantId());
            cartItem.setSoLuong(finalQty);
            cartItemRepository.save(cartItem);
        }
        savedRepository.deleteById(savedId);
        return true;
    }

    @Transactional
    public void removeSaved(Integer userId, Integer savedId) {
        savedRepository.deleteByIdAndUserId(savedId, userId);
    }

    public int count(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return savedRepository.countByUserId(userId);
    }

    private CartItemDTO toDto(SavedCartItem item) {
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
        dto.setNgayThem(item.getNgayLuu());
        return dto;
    }
}
