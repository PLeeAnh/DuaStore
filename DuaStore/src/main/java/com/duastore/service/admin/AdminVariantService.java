package com.duastore.service.admin;

import com.duastore.dto.ProductVariantFormDTO;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminVariantService {

    private final ProductVariantRepository variantRepository;
    private final FileUploadService fileUploadService;

    public AdminVariantService(ProductVariantRepository variantRepository, FileUploadService fileUploadService) {
        this.variantRepository = variantRepository;
        this.fileUploadService = fileUploadService;
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

    public List<Integer> getDistinctDungTich() {
        return variantRepository.findDistinctDungTich();
    }

    public List<ProductVariant> searchByProductId(Integer productId, String keyword) {
        return variantRepository.searchByProductId(productId, keyword);
    }

    public ProductVariant findById(Integer id) {
        return variantRepository.findById(id).orElse(null);
    }

    public ProductVariant save(ProductVariantFormDTO dto) {
        ProductVariant v = (dto.getId() != null) ? variantRepository.findById(dto.getId()).orElse(new ProductVariant()) : new ProductVariant();

        v.setProductId(dto.getProductId());
        v.setTenBienThe(dto.getTenBienThe());
        v.setDungTich(dto.getDungTich());
        v.setGiaGoc(dto.getGiaGoc());
        v.setGiaKhuyenMai(dto.getGiaKhuyenMai());
        v.setSoLuongTon(dto.getSoLuongTon());
        v.setDefault(dto.isDefault());

        String uploaded = fileUploadService.save(dto.getHinhAnhFile());
        if (uploaded != null) v.setHinhAnh(uploaded);

        if (dto.isDefault()) {
            List<ProductVariant> others = variantRepository.findByProductIdAndIsActiveTrue(dto.getProductId());
            others.forEach(o -> { if (!o.getId().equals(v.getId())) o.setDefault(false); });
            variantRepository.saveAll(others);
        }

        return variantRepository.save(v);
    }

    public void delete(Integer id) {
        ProductVariant v = variantRepository.findById(id).orElse(null);
        if (v != null) {
            v.setActive(false);
            variantRepository.save(v);
        }
    }
}
