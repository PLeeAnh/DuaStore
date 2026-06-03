package com.duastore.service.client;

import com.duastore.dto.VariantApiDTO;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public List<Product> getFeatured() {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue();
    }

    public List<Product> getDangBan() {
        return productRepository.findDangBan();
    }

    public List<Product> search(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public List<Product> findByCategory(Integer danhMucId) {
        return productRepository.findByDanhMucIdAndIsActiveTrue(danhMucId);
    }

    public Product findById(Integer id) {
        Product p = productRepository.findById(id).orElse(null);
        if (p != null && !p.isActive()) return null;
        return p;
    }

    public List<ProductVariant> getVariants(Integer productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public VariantApiDTO getVariantApi(Integer variantId) {
        ProductVariant v = variantRepository.findById(variantId).orElse(null);
        if (v == null) return null;
        VariantApiDTO dto = new VariantApiDTO();
        dto.setId(v.getId());
        dto.setTenBienThe(v.getTenBienThe());
        dto.setGiaGoc(v.getGiaGoc());
        dto.setGiaKhuyenMai(v.getGiaKhuyenMai());
        dto.setSoLuongTon(v.getSoLuongTon());
        dto.setHinhAnh(v.getHinhAnh());
        dto.setConHang(v.getSoLuongTon() > 0);
        return dto;
    }
}
