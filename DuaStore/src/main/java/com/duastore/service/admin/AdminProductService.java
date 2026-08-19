package com.duastore.service.admin;

import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final FileUploadService fileUploadService;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;

    public AdminProductService(ProductRepository productRepository,
            FileUploadService fileUploadService,
            ProductImageRepository productImageRepository,
            CategoryRepository categoryRepository,
            ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.fileUploadService = fileUploadService;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.productVariantRepository = productVariantRepository;
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
        return distinctValuesOrderedByUsage(productRepository.findThuongHieuCounts());
    }

    public List<String> getDistinctChatLieu() {
        return distinctValuesOrderedByUsage(productRepository.findChatLieuCounts());
    }

    public List<String> getDistinctXuatXu() {
        return distinctValuesOrderedByUsage(productRepository.findXuatXuCounts());
    }

    public List<String> getDistinctKinhLoai() {
        return distinctValuesOrderedByUsage(productRepository.findKinhLoaiCounts());
    }

    public List<String> getDistinctMucDichSuDung() {
        return distinctValuesOrderedByUsage(productRepository.findMucDichSuDungCounts());
    }

    private List<String> distinctValuesOrderedByUsage(List<Object[]> rows) {
        if (rows == null) return new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                values.add(String.valueOf(row[0]));
            }
        }
        return values;
    }

    public Map<Integer, Integer> getTotalStockMap(List<Product> products) {
        Map<Integer, Integer> map = new HashMap<>();
        if (products.isEmpty()) return map;
        List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findByProductIdInAndIsActiveTrue(ids);
        for (ProductVariant v : variants) {
            map.merge(v.getProductId(), v.getSoLuongTon(), Integer::sum);
        }
        return map;
    }

    public List<Category> buildCategoryBreadcrumb(Integer categoryId) {
        List<Category> path = new ArrayList<>();
        Integer id = categoryId;
        while (id != null) {
            Category cat = categoryRepository.findById(id).orElse(null);
            if (cat == null) break;
            path.add(0, cat);
            id = cat.getParent() != null ? cat.getParent().getId() : null;
        }
        return path;
    }

    public Map<Integer, String> getCategoryMap(List<Category> categories) {
        return categories.stream().collect(Collectors.toMap(Category::getId, Category::getTenDanhMuc));
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public String getCategoryName(Integer categoryId) {
        Category cat = categoryRepository.findById(categoryId).orElse(null);
        return cat != null ? cat.getTenDanhMuc() : "—";
    }

    public int getTotalStockForProduct(Integer productId) {
        return productVariantRepository.findByProductIdAndIsActiveTrue(productId)
                .stream().mapToInt(ProductVariant::getSoLuongTon).sum();
    }

    public BigDecimal getMinPrice(List<ProductVariant> variants) {
        return variants.stream()
                .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public BigDecimal getMaxPrice(List<ProductVariant> variants) {
        return variants.stream()
                .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
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
        p.setMoTa(com.duastore.util.HtmlSanitizer.sanitize(dto.getMoTa()));
        p.setChatLieu(dto.getChatLieu());
        p.setXuatXu(dto.getXuatXu());
        p.setMucDichSuDung(dto.getMucDichSuDung());
        p.setThuongHieu(dto.getThuongHieu());
        p.setKinhLoai(dto.getKinhLoai());
        p.setDanhMucId(dto.getDanhMucId());
        p.setTrangThaiSanPham(dto.getTrangThaiSanPham());
        p.setLeadTimeDays(dto.getLeadTimeDays());
        p.setFeatured(dto.isFeatured());
        p.setNgayPhatHanh(dto.getNgayPhatHanh());

        String oldMainImage = p.getHinhAnhChinh();
        String uploaded = fileUploadService.save(dto.getHinhAnhFile());
        if (uploaded != null) {
            p.setHinhAnhChinh(uploaded);
        } else if (dto.getId() == null && dto.getHinhAnhChinh() != null) {
            p.setHinhAnhChinh(dto.getHinhAnhChinh());
        }

        Product saved = productRepository.save(p);
        if (uploaded != null && oldMainImage != null && !oldMainImage.equals(uploaded)) {
            fileUploadService.deleteAfterCommit(oldMainImage);
        }

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
            String mainImage = p.getHinhAnhChinh();
            List<String> galleryImages = productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id)
                    .stream().map(ProductImage::getImageUrl).toList();
            List<String> variantImages = p.getVariants() != null
                    ? p.getVariants().stream().map(ProductVariant::getHinhAnh).filter(Objects::nonNull).toList()
                    : List.of();

            p.setActive(false);
            if (p.getVariants() != null) {
                p.getVariants().forEach(v -> v.setActive(false));
            }
            productRepository.save(p);

            fileUploadService.deleteAfterCommit(mainImage);
            galleryImages.forEach(fileUploadService::deleteAfterCommit);
            variantImages.forEach(fileUploadService::deleteAfterCommit);
        }
    }
}
