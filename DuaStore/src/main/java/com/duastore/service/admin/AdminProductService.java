package com.duastore.service.admin;

import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;
    private final ProductImageRepository productImageRepository;

    public AdminProductService(ProductRepository productRepository,
            FileUploadService fileUploadService,
            ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
        this.productImageRepository = productImageRepository;
    }

    public List<Product> findAll() {
        return productRepository.findByIsActiveTrueOrderByNgayTaoDesc();
    }

    public Page<Product> findAllPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByIsActiveTrueOrderByNgayTaoDesc(pageable);
    }

    public List<Product> search(String keyword, Integer danhMucId, String trangThai) {
        return productRepository.searchWithFilters(keyword, danhMucId, trangThai);
    }

    public Page<Product> searchPaged(String keyword, Integer danhMucId, String trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.searchWithFiltersPaged(keyword, danhMucId, trangThai, pageable);
    }

    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<String> getDistinctThuongHieu() {
        return productRepository.findDistinctThuongHieu();
    }

    public List<String> getDistinctChatLieu() {
        return productRepository.findDistinctChatLieu();
    }

    public List<String> getDistinctXuatXu() {
        return productRepository.findDistinctXuatXu();
    }

    public List<String> getDistinctKinhLoai() {
        return productRepository.findDistinctKinhLoai();
    }

    public List<String> getDistinctMucDichSuDung() {
        return productRepository.findDistinctMucDichSuDung();
    }

    @Transactional
    public Product save(ProductFormDTO dto) {
        Product p;
        if (dto.getId() != null) {
            p = productRepository.findById(dto.getId()).orElse(null);
            if (p == null) {
                return null;
            }
        } else {
            p = new Product();
        }

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

        Product saved = productRepository.save(p);

        // Save gallery images
        if (dto.getGalleryFiles() != null) {
            int order = productImageRepository
                    .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(saved.getId())
                    .size();
            for (MultipartFile file : dto.getGalleryFiles()) {
                if (!file.isEmpty()) {
                    String url = fileUploadService.save(file);
                    if (url != null) {
                        ProductImage pi = new ProductImage();
                        pi.setProductId(saved.getId());
                        pi.setImageUrl(url);
                        pi.setSortOrder(order++);
                        productImageRepository.save(pi);
                    }
                }
            }
        }

        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        Product p = productRepository.findById(id).orElse(null);
        if (p != null) {
            p.setActive(false);
            productRepository.save(p);
        }
    }
}
