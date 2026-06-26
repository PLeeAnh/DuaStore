package com.duastore.controller.admin;

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
import com.duastore.service.admin.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/san-pham")
public class AdminProductController {

    private static final int LOW_STOCK_THRESHOLD = 20;

    private final AdminProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FileUploadService fileUploadService;

    public AdminProductController(AdminProductService productService,
                                   CategoryRepository categoryRepository,
                                   ProductRepository productRepository,
                                   ProductImageRepository productImageRepository,
                                   ProductVariantRepository productVariantRepository,
                                   FileUploadService fileUploadService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (trangThai != null && trangThai.isBlank()) trangThai = null;

        boolean hasFilter = (keyword != null)
                         || danhMuc != null
                         || (trangThai != null);

        Page<Product> productPage;
        if (hasFilter) {
            productPage = productService.searchPaged(keyword, danhMuc, trangThai, page, size);
        } else {
            productPage = productService.findAllPaged(page, size);
        }
        model.addAttribute("products", productPage.getContent());

        model.addAttribute("keyword", keyword);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("trangThai", trangThai);
        List<Category> cats = categoryRepository.findByIsActiveTrue();
        model.addAttribute("categories", cats);
        model.addAttribute("categoryMap", cats.stream().collect(Collectors.toMap(Category::getId, Category::getTenDanhMuc)));

        Map<Integer, Integer> totalStock = new HashMap<>();
        List<Product> products = productPage.getContent();
        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> allVariants = productVariantRepository.findByProductIdInAndIsActiveTrue(ids);
            for (ProductVariant v : allVariants) {
                totalStock.merge(v.getProductId(), v.getSoLuongTon(), Integer::sum);
            }
        }
        model.addAttribute("totalStock", totalStock);

        Map<String, Object> filterParams = new HashMap<>();
        if (keyword != null) filterParams.put("keyword", keyword);
        if (danhMuc != null) filterParams.put("danhMuc", danhMuc);
        if (trangThai != null) filterParams.put("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "sản phẩm");
        model.addAttribute("url", "/admin/san-pham");
        model.addAttribute("filterParams", filterParams);

        model.addAttribute("totalProducts", productRepository.countByIsActiveTrue());
        model.addAttribute("featuredCount", productRepository.countByIsFeaturedTrueAndIsActiveTrue());
        model.addAttribute("lowStockCount", productVariantRepository.countLowStockProducts(LOW_STOCK_THRESHOLD));
        model.addAttribute("totalStockAll", productVariantRepository.sumTotalStock());

        return "view/admin/product/product-list";
    }

    @GetMapping("/chi-tiet/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Product p = productService.findById(id);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }

        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", p);

        Category cat = categoryRepository.findById(p.getDanhMucId()).orElse(null);
        model.addAttribute("categoryName", cat != null ? cat.getTenDanhMuc() : "—");

        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(p.getId());
        model.addAttribute("variants", variants);

        List<ProductImage> gallery = productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(p.getId());
        model.addAttribute("galleryImages", gallery);

        int totalStock = variants.stream().mapToInt(ProductVariant::getSoLuongTon).sum();
        model.addAttribute("totalStock", totalStock);

        BigDecimal minPrice = variants.stream()
            .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
            .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = variants.stream()
            .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
            .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "view/admin/product/product-detail";
    }

    @GetMapping("/bien-the")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String variantPage(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "bien-the");

        boolean hasFilter = keyword != null && !keyword.isBlank();
        Page<ProductVariant> variantPage;
        if (hasFilter) {
            variantPage = productVariantRepository.searchAllPaged(keyword, PageRequest.of(page, size));
        } else {
            variantPage = productVariantRepository.findByIsActiveTrueOrderByIdAsc(PageRequest.of(page, size));
        }

        Map<Integer, String> productNames = new HashMap<>();
        for (ProductVariant v : variantPage.getContent()) {
            if (!productNames.containsKey(v.getProductId())) {
                Product p = productService.findById(v.getProductId());
                productNames.put(v.getProductId(), p != null ? p.getTenSanPham() : "Đã xóa");
            }
        }

        Map<String, Object> filterParams = new HashMap<>();
        if (hasFilter) filterParams.put("keyword", keyword);

        model.addAttribute("variants", variantPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", variantPage.getTotalPages());
        model.addAttribute("totalItems", variantPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "biến thể");
        model.addAttribute("url", "/admin/san-pham/bien-the");
        model.addAttribute("filterParams", filterParams);

        model.addAttribute("totalVariants", productVariantRepository.countByIsActiveTrue());
        model.addAttribute("lowStockVariants", productVariantRepository.countByIsActiveTrueAndSoLuongTonLessThanEqual(LOW_STOCK_THRESHOLD));
        model.addAttribute("totalStockAll", productVariantRepository.sumTotalStock());

        return "view/admin/product/variant-page";
    }

    @GetMapping("/hinh-anh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String imagePage(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "hinh-anh");

        Page<Product> productPage = productService.findAllPaged(page, size);
        Map<Integer, Integer> imageCounts = new HashMap<>();
        for (Product p : productPage.getContent()) {
            imageCounts.put(p.getId(),
                productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(p.getId()).size());
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("imageCounts", imageCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "sản phẩm");
        model.addAttribute("url", "/admin/san-pham/hinh-anh");
        model.addAttribute("filterParams", new HashMap<>());

        long totalImages = productImageRepository.countByIsActiveTrue();
        long productsWithImages = productImageRepository.countProductsWithImages();
        long totalProducts = productRepository.countByIsActiveTrue();
        model.addAttribute("totalImages", totalImages);
        model.addAttribute("productsWithImages", productsWithImages);
        model.addAttribute("productsWithoutImages", totalProducts - productsWithImages);

        return "view/admin/product/image-page";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", new ProductFormDTO());
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        addComboLists(model);
        return "view/admin/product/product-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String create(@Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            addComboLists(model);
            return "view/admin/product/product-form";
        }
        productService.save(dto);
        ra.addFlashAttribute("successMsg", "Thêm sản phẩm thành công");
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Product p = productService.findById(id);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }

        ProductFormDTO dto = new ProductFormDTO();
        dto.setId(p.getId());
        dto.setTenSanPham(p.getTenSanPham());
        dto.setMoTa(p.getMoTa());
        dto.setChatLieu(p.getChatLieu());
        dto.setXuatXu(p.getXuatXu());
        dto.setMucDichSuDung(p.getMucDichSuDung());
        dto.setThuongHieu(p.getThuongHieu());
        dto.setKinhLoai(p.getKinhLoai());
        dto.setDanhMucId(p.getDanhMucId());
        dto.setHinhAnhChinh(p.getHinhAnhChinh());
        dto.setTrangThaiSanPham(p.getTrangThaiSanPham());
        dto.setLeadTimeDays(p.getLeadTimeDays());
        dto.setFeatured(p.isFeatured());

        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        model.addAttribute("galleryImages", productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
        Category cat = categoryRepository.findById(p.getDanhMucId()).orElse(null);
        model.addAttribute("categoryName", cat != null ? cat.getTenDanhMuc() : "—");
        addComboLists(model);
        return "view/admin/product/product-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            model.addAttribute("galleryImages", productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
            Category cat = categoryRepository.findById(dto.getDanhMucId()).orElse(null);
            model.addAttribute("categoryName", cat != null ? cat.getTenDanhMuc() : "—");
            addComboLists(model);
            return "view/admin/product/product-form";
        }
        dto.setId(id);
        Product saved = productService.save(dto);
        if (saved == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        ra.addFlashAttribute("successMsg", "Cập nhật sản phẩm thành công");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("successMsg", "Đã xóa sản phẩm");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/chi-tiet/{id}/anh-chinh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String updateMainImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        Product p = productService.findById(id);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        String uploaded = fileUploadService.save(file);
        if (uploaded != null) {
            p.setHinhAnhChinh(uploaded);
            productRepository.save(p);
            ra.addFlashAttribute("successMsg", "Cập nhật ảnh đại diện thành công");
        }
        return "redirect:/admin/san-pham/chi-tiet/" + id;
    }

    @PostMapping("/chi-tiet/{id}/them-anh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String addGalleryImages(@PathVariable Integer id, @RequestParam("files") List<MultipartFile> files, RedirectAttributes ra) {
        Product p = productService.findById(id);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        int order = productImageRepository
            .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id)
            .size();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = fileUploadService.save(file);
                if (url != null) {
                    ProductImage img = new ProductImage();
                    img.setProductId(id);
                    img.setImageUrl(url);
                    img.setSortOrder(order++);
                    productImageRepository.save(img);
                }
            }
        }
        ra.addFlashAttribute("successMsg", "Thêm ảnh thành công");
        return "redirect:/admin/san-pham/chi-tiet/" + id;
    }

    @PostMapping("/xoa-anh/{imageId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String deleteImage(@PathVariable Integer imageId, RedirectAttributes ra) {
        ProductImage img = productImageRepository.findById(imageId).orElse(null);
        if (img == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy ảnh");
            return "redirect:/admin/san-pham";
        }
        Integer productId = img.getProductId();
        img.setActive(false);
        productImageRepository.save(img);
        ra.addFlashAttribute("successMsg", "Đã xóa ảnh");
        return "redirect:/admin/san-pham/chi-tiet/" + productId;
    }

    @PostMapping("/chi-tiet/{id}/xoa-anh-chinh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    @ResponseBody
    public ResponseEntity<?> clearMainImage(@PathVariable Integer id) {
        Product p = productService.findById(id);
        if (p == null) return ResponseEntity.notFound().build();
        p.setHinhAnhChinh(null);
        productRepository.save(p);
        return ResponseEntity.ok().build();
    }

    private void addComboLists(Model model) {
        model.addAttribute("brands", productService.getDistinctThuongHieu());
        model.addAttribute("materials", productService.getDistinctChatLieu());
        model.addAttribute("origins", productService.getDistinctXuatXu());
        model.addAttribute("glassTypes", productService.getDistinctKinhLoai());
        model.addAttribute("purposes", productService.getDistinctMucDichSuDung());
    }

}
