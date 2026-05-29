package com.duastore.service.admin;

import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Product;
import com.duastore.repository.ProductRepository;
import com.duastore.service.FileUploadService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;

    public AdminProductService(ProductRepository productRepository, FileUploadService fileUploadService) {
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<Product> findAll() {
        return productRepository.findByIsActiveTrueOrderByNgayTaoDesc();
    }

    public List<Product> search(String keyword, Integer danhMucId, String trangThai) {
        return productRepository.searchWithFilters(keyword, danhMucId, trangThai);
    }

    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product save(ProductFormDTO dto) {
        Product p = (dto.getId() != null) ? productRepository.findById(dto.getId()).orElse(new Product()) : new Product();

        p.setTenSanPham(dto.getTenSanPham());
        p.setMoTa(dto.getMoTa());
        p.setChatLieu(dto.getChatLieu());
        p.setXuatXu(dto.getXuatXu());
        p.setMucDichSuDung(dto.getMucDichSuDung());
        p.setThuongHieu(dto.getThuongHieu());
        p.setKinhLoai(dto.getKinhLoai());
        p.setDanhMucId(dto.getDanhMucId());
        p.setTrangThaiSanPham(dto.getTrangThaiSanPham());
        p.setLeadTimeDays(dto.getLeadTimeDays());
        p.setFeatured(dto.isFeatured());

        String uploaded = fileUploadService.save(dto.getHinhAnhFile());
        if (uploaded != null) {
            p.setHinhAnhChinh(uploaded);
        } else if (dto.getId() == null && dto.getHinhAnhChinh() != null) {
            p.setHinhAnhChinh(dto.getHinhAnhChinh());
        }

        return productRepository.save(p);
    }

    public void delete(Integer id) {
        Product p = productRepository.findById(id).orElse(null);
        if (p != null) {
            p.setActive(false);
            productRepository.save(p);
        }
    }
}
