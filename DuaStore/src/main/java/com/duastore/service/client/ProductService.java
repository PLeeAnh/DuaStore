package com.duastore.service.client;

import com.duastore.dto.VariantApiDTO;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Cacheable(value = "featuredProducts", unless = "#result.isEmpty()")
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

    public List<Product> findByCategories(List<Integer> danhMucIds) {
        if (danhMucIds == null || danhMucIds.isEmpty()) {
            return List.of();
        }
        return productRepository.findByDanhMucIdInAndIsActiveTrue(danhMucIds);
    }

    public Page<Product> getDangBanPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findDangBanPaged(pageable);
    }

    public Page<Product> searchPaged(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchByNamePaged(keyword, pageable);
    }

    public Page<Product> findByCategoriesPaged(List<Integer> danhMucIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByDanhMucIdInAndIsActiveTrue(danhMucIds, pageable);
    }

    public Page<Product> filterPaged(String keyword, Integer danhMucId,
                                      String chatLieu, String priceRange,
                                      Integer dungTich, String sortBy,
                                      int page, int size) {
        BigDecimal[] price = parsePriceRange(priceRange);
        BigDecimal minPrice = price != null ? price[0] : null;
        BigDecimal maxPrice = price != null ? price[1] : null;
        Pageable pageable = buildPageable(sortBy, page, size);
        return productRepository.filterPaged(keyword, danhMucId, minPrice, maxPrice, dungTich, chatLieu, pageable);
    }

    public List<String> getDistinctShapes() {
        return productRepository.findDistinctHinhDang();
    }

    public List<String> getDistinctChatLieu() {
        return productRepository.findDistinctChatLieu();
    }

    public BigDecimal[] parsePriceRange(String code) {
        if (code == null) return null;
        return switch (code) {
            case "lt100" -> new BigDecimal[]{null, new BigDecimal("100000")};
            case "r100_200" -> new BigDecimal[]{new BigDecimal("100000"), new BigDecimal("200000")};
            case "r200_500" -> new BigDecimal[]{new BigDecimal("200000"), new BigDecimal("500000")};
            case "r500_1000" -> new BigDecimal[]{new BigDecimal("500000"), new BigDecimal("1000000")};
            case "gt1000" -> new BigDecimal[]{new BigDecimal("1000000"), null};
            default -> null;
        };
    }

    private Pageable buildPageable(String sortBy, int page, int size) {
        Sort sort;
        if ("name_asc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "tenSanPham");
        } else if ("name_desc".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "tenSanPham");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "ngayTao");
        }
        return PageRequest.of(page, size, sort);
    }

    public List<Integer> getDistinctVolumes() {
        return variantRepository.findDistinctDungTich();
    }

    public List<String> getDistinctCapTypes() {
        List<String> tenBienTheList = variantRepository.findDistinctTenBienThe();
        Set<String> kieuNaps = new LinkedHashSet<>();
        for (String name : tenBienTheList) {
            if (name != null && name.contains(" - ")) {
                String[] parts = name.split("\\s*-\\s*");
                if (parts.length >= 2) {
                    String cap = parts[1].trim();
                    if (cap.toLowerCase(java.util.Locale.ROOT).contains("nắp"))
                        kieuNaps.add(cap);
                }
            }
        }
        return new ArrayList<>(kieuNaps);
    }

    public Product findById(Integer id) {
        Product p = productRepository.findById(id).orElse(null);
        if (p != null && !p.isActive()) return null;
        return p;
    }

    public List<ProductVariant> getVariants(Integer productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public List<Product> getRelatedProducts(Integer productId, Integer danhMucId, int limit) {
        return productRepository.findByDanhMucIdAndIsActiveTrue(danhMucId).stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit)
                .collect(Collectors.toList());
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
