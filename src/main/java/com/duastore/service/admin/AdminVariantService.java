package com.duastore.service.admin;

import com.duastore.dto.ProductVariantFormDTO;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.FileUploadService;
import com.duastore.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý biến thể sản phẩm.
 */
public class AdminVariantService {

    private final ProductVariantRepository variantRepository;
    private final FileUploadService fileUploadService;
    private final PriceHistoryService priceHistoryService;
    private final PricingService pricingService;

    public AdminVariantService(ProductVariantRepository variantRepository, FileUploadService fileUploadService,
                               PriceHistoryService priceHistoryService, PricingService pricingService) {
        this.variantRepository = variantRepository;
        this.fileUploadService = fileUploadService;
        this.priceHistoryService = priceHistoryService;
        this.pricingService = pricingService;
    }

    public Page<ProductVariant> findAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return variantRepository.findByIsActiveTrueOrderByIdAsc(pageable);
    }

    public Page<ProductVariant> searchAllPaged(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return variantRepository.searchAllPaged(keyword, pageable);
    }

    public List<ProductVariant> findByProductId(Integer productId) {
        return variantRepository.findByProductIdAndIsActiveTrue(productId);
    }

    public Page<ProductVariant> findByProductIdPaged(Integer productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return variantRepository.findByProductIdAndIsActiveTrue(productId, pageable);
    }

    public List<Integer> getDistinctDungTich() {
        return variantRepository.findDistinctDungTich();
    }

    public List<ProductVariant> searchByProductId(Integer productId, String keyword) {
        return variantRepository.searchByProductId(productId, keyword);
    }

    public ProductVariant findById(Integer id) {
        return variantRepository.findById(id).orElse(null);
    }

    @Transactional
    public ProductVariant save(ProductVariantFormDTO dto) {
        ProductVariant v = (dto.getId() != null) ? variantRepository.findById(dto.getId()).orElse(new ProductVariant()) : new ProductVariant();

        BigDecimal oldPrice = v.getGiaGoc();

        v.setProductId(dto.getProductId());
        v.setTenBienThe(dto.getTenBienThe());
        v.setDungTich(dto.getDungTich());
        v.setGiaGoc(dto.getGiaGoc());
        v.setGiaKhuyenMai(dto.getGiaKhuyenMai());
        v.setSoLuongTon(dto.getSoLuongTon());
        v.setGiaVon(dto.getGiaVon());
        v.setLowStockThreshold(dto.getLowStockThreshold() != null ? dto.getLowStockThreshold() : 20);
        v.setDefault(dto.isDefault());

        String oldImage = v.getHinhAnh();
        String uploaded = fileUploadService.save(dto.getHinhAnhFile());
        if (uploaded != null) {
            v.setHinhAnh(uploaded);
        }

        if (dto.isDefault()) {
            List<ProductVariant> others = variantRepository.findByProductIdAndIsActiveTrue(dto.getProductId());
            others.forEach(o -> {
                if (!o.getId().equals(v.getId())) {
                    o.setDefault(false);
                }
            });
            variantRepository.saveAll(others);
        }

        ProductVariant saved = variantRepository.save(v);
        pricingService.recalculateMinPrice(saved.getProductId());
        if (uploaded != null && oldImage != null && !oldImage.equals(uploaded)) {
            fileUploadService.deleteAfterCommit(oldImage);
        }

        if (oldPrice != null && dto.getGiaGoc() != null && oldPrice.compareTo(dto.getGiaGoc()) != 0) {
            priceHistoryService.record(saved.getId(), saved.getTenBienThe(), saved.getProductId(),
                    null, oldPrice, dto.getGiaGoc(), null, "ADMIN");
        }

        return saved;
    }

    @Transactional
    public void bulkUpdate(List<Map<String, Object>> variants, Integer adminId) {
        for (Map<String, Object> entry : variants) {
            Integer id = (Integer) entry.get("id");
            if (id == null) continue;

            ProductVariant v = variantRepository.findById(id).orElse(null);
            if (v == null) continue;

            BigDecimal oldPrice = v.getGiaGoc();

            if (entry.containsKey("giaBan")) {
                Object giaBanObj = entry.get("giaBan");
                if (giaBanObj instanceof Number) {
                    v.setGiaGoc(new BigDecimal(((Number) giaBanObj).doubleValue()));
                }
            }
            if (entry.containsKey("soLuongTon")) {
                Object soLuongObj = entry.get("soLuongTon");
                if (soLuongObj instanceof Number) {
                    v.setSoLuongTon(((Number) soLuongObj).intValue());
                }
            }
            if (entry.containsKey("giaVon")) {
                Object giaVonObj = entry.get("giaVon");
                if (giaVonObj instanceof Number) {
                    v.setGiaVon(new BigDecimal(((Number) giaVonObj).doubleValue()));
                }
            }
            if (entry.containsKey("lowStockThreshold")) {
                Object thresholdObj = entry.get("lowStockThreshold");
                if (thresholdObj instanceof Number) {
                    v.setLowStockThreshold(((Number) thresholdObj).intValue());
                }
            }

            variantRepository.save(v);
            pricingService.recalculateMinPrice(v.getProductId());

            if (oldPrice != null && v.getGiaGoc() != null && oldPrice.compareTo(v.getGiaGoc()) != 0) {
                String productName = v.getProduct() != null ? v.getProduct().getTenSanPham() : null;
                priceHistoryService.record(v.getId(), v.getTenBienThe(), v.getProductId(),
                        productName, oldPrice, v.getGiaGoc(), adminId, "ADMIN");
            }
        }
    }

    @Transactional
    public void delete(Integer id) {
        ProductVariant v = variantRepository.findById(id).orElse(null);
        if (v != null) {
            Integer productId = v.getProductId();
            String imageUrl = v.getHinhAnh();
            v.setActive(false);
            variantRepository.save(v);
            pricingService.recalculateMinPrice(productId);
            fileUploadService.deleteAfterCommit(imageUrl);
        }
    }
}
