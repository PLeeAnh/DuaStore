package com.duastore.service.client;

import com.duastore.dto.WishlistDTO;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.Wishlist;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.WishlistRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           ProductRepository productRepository,
                           ProductVariantRepository variantRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<WishlistDTO> getWishlistByUser(Integer userId) {
        return wishlistRepository.findByUserIdOrderByNgayThemDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<Integer> getLikedProductIds(Integer userId) {
        return wishlistRepository.findProductIdsByUserId(userId);
    }

    public boolean isLiked(Integer userId, Integer productId) {
        if (userId == null) return false;
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public boolean toggle(Integer userId, Integer productId) {
        if (productId == null) return false;
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return false;
        }
        Wishlist wish = new Wishlist();
        wish.setUserId(userId);
        wish.setProductId(productId);
        try {
            wishlistRepository.save(wish);
        } catch (DataIntegrityViolationException e) {
            return false;
        }
        return true;
    }

    private WishlistDTO toDto(Wishlist wish) {
        WishlistDTO dto = new WishlistDTO();
        dto.setId(wish.getId());
        dto.setUserId(wish.getUserId());
        dto.setProductId(wish.getProductId());
        dto.setNgayThem(wish.getNgayThem());
        if (wish.getProduct() != null) {
            Product p = wish.getProduct();
            dto.setTenSanPham(p.getTenSanPham());
            dto.setHinhAnh(p.getHinhAnhChinh());
            List<ProductVariant> variants = variantRepository.findByProductIdAndIsActiveTrue(p.getId());
            if (!variants.isEmpty()) {
                dto.setGiaBan(variants.get(0).getGiaKhuyenMai() != null ? variants.get(0).getGiaKhuyenMai() : variants.get(0).getGiaGoc());
                dto.setGiaGoc(variants.get(0).getGiaGoc());
            }
        }
        return dto;
    }
}
