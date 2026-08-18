package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.model.User;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.WishlistRepository;
import com.duastore.service.FileUploadService;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminProductService;
import com.duastore.service.admin.AutoDescriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/san-pham")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);
    private static final int LOW_STOCK_THRESHOLD = 20;

    private final AdminProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WishlistRepository wishlistRepository;
    private final FileUploadService fileUploadService;
    private final NotificationHelper notificationHelper;
    private final SecurityUtil securityUtil;
    private final AutoDescriptionService autoDescriptionService;

    public AdminProductController(AdminProductService productService,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ProductVariantRepository productVariantRepository,
            WishlistRepository wishlistRepository,
            FileUploadService fileUploadService,
            NotificationHelper notificationHelper,
            SecurityUtil securityUtil,
            AutoDescriptionService autoDescriptionService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.wishlistRepository = wishlistRepository;
        this.fileUploadService = fileUploadService;
        this.notificationHelper = notificationHelper;
        this.securityUtil = securityUtil;
        this.autoDescriptionService = autoDescriptionService;
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

        boolean hasFilter = keyword != null || danhMuc != null || trangThai != null;
        Page<Product> productPage = hasFilter
                ? productService.searchPaged(keyword, danhMuc, trangThai, page, size)
                : productService.findAllPaged(page, size);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("trangThai", trangThai);

        if (danhMuc != null) {
            model.addAttribute("categoryBreadcrumb", productService.buildCategoryBreadcrumb(danhMuc));
        }

        List<Category> cats = productService.getActiveCategories();
        model.addAttribute("categories", cats);
        model.addAttribute("categoryMap", productService.getCategoryMap(cats));
        model.addAttribute("totalStock", productService.getTotalStockMap(productPage.getContent()));

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
        model.addAttribute("categoryName", productService.getCategoryName(p.getDanhMucId()));

        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsActiveTrue(p.getId());
        model.addAttribute("variants", variants);
        model.addAttribute("totalStock", variants.stream().mapToInt(ProductVariant::getSoLuongTon).sum());

        List<ProductImage> gallery = productImageRepository
                .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(p.getId());
        model.addAttribute("galleryImages", gallery);

        model.addAttribute("minPrice", productService.getMinPrice(variants));
        model.addAttribute("maxPrice", productService.getMaxPrice(variants));

        return "view/admin/product/product-detail";
    }

    @GetMapping("/bien-the")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String variantPage(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") boolean lowStock,
            Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "bien-the");

        boolean hasFilter = (keyword != null && !keyword.isBlank()) || lowStock;
        Page<ProductVariant> variantPage;
        if (lowStock) {
            variantPage = productVariantRepository.findByIsActiveTrueAndSoLuongTonLessThanEqualOrderByIdAsc(LOW_STOCK_THRESHOLD, PageRequest.of(page, size));
        } else if (hasFilter) {
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
        if (lowStock) filterParams.put("lowStock", "1");

        model.addAttribute("variants", variantPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("keyword", keyword);
        model.addAttribute("lowStock", lowStock);
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
    public String createForm(@RequestParam(required = false) String tenSanPham, Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        ProductFormDTO dto = new ProductFormDTO();
        if (tenSanPham != null && !tenSanPham.isBlank()) {
            dto.setTenSanPham(tenSanPham.trim());
        }
        model.addAttribute("product", dto);
        model.addAttribute("categories", productService.getActiveCategories());
        addComboLists(model);
        return "view/admin/product/product-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String create(@Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("product", dto);
            model.addAttribute("categories", productService.getActiveCategories());
            addComboLists(model);
            return "view/admin/product/product-form";
        }
        Product saved = productService.save(dto);
        if (saved != null) {
            try {
                notificationHelper.notifyAll(
                        "Sản phẩm mới: " + saved.getTenSanPham(),
                        "PRODUCT", saved.getId(),
                        "/san-pham/" + saved.getId(),
                        saved.getTenSanPham()
                );
            } catch (Exception e) {
                log.warn("Loi gui thong bao san pham moi: {}", e.getMessage());
            }
            notificationHelper.notifyStaff(
                    "Admin " + getCurrentAdminName() + " đã thêm sản phẩm mới: " + saved.getTenSanPham(),
                    "PRODUCT", saved.getId(),
                    "/admin/san-pham/chi-tiet/" + saved.getId(),
                    "Xem sản phẩm"
            );
        }
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
        dto.setNgayPhatHanh(p.getNgayPhatHanh());

        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", dto);
        model.addAttribute("categories", productService.getActiveCategories());
        model.addAttribute("galleryImages", productImageRepository
                .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
        model.addAttribute("categoryName", productService.getCategoryName(p.getDanhMucId()));
        model.addAttribute("ngayTao", p.getNgayTao());
        model.addAttribute("ngayCapNhat", p.getNgayCapNhat());
        addComboLists(model);
        return "view/admin/product/product-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("product", dto);
            model.addAttribute("categories", productService.getActiveCategories());
            model.addAttribute("galleryImages", productImageRepository
                    .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
            Category cat = categoryRepository.findById(dto.getDanhMucId()).orElse(null);
            model.addAttribute("categoryName", cat != null ? cat.getTenDanhMuc() : "—");
            addComboLists(model);
            return "view/admin/product/product-form";
        }
        dto.setId(id);
        List<ProductVariant> oldVariants = productVariantRepository.findByProductIdAndIsActiveTrue(id);
        Map<Integer, Integer> oldStock = oldVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, ProductVariant::getSoLuongTon));
        Map<Integer, BigDecimal> oldPrices = oldVariants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, this::effectivePrice));
        Product saved = productService.save(dto);
        if (saved == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        ra.addFlashAttribute("successMsg", "Cập nhật sản phẩm thành công");
        notifyWishlistUsersOnVariantChanges(saved, oldStock, oldPrices);
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("successMsg", "Đã xóa sản phẩm");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/xoa-hang-loat")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String bulkDelete(@RequestParam("ids") List<Integer> ids, RedirectAttributes ra) {
        for (Integer id : ids) {
            productService.delete(id);
        }
        ra.addFlashAttribute("successMsg", "Đã xóa " + ids.size() + " sản phẩm");
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
                .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id).size();
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
        fileUploadService.deleteAfterCommit(img.getImageUrl());
        ra.addFlashAttribute("successMsg", "Đã xóa ảnh");
        return "redirect:/admin/san-pham/chi-tiet/" + productId;
    }

    @PostMapping("/chi-tiet/{id}/xoa-anh-chinh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    @ResponseBody
    public ResponseEntity<?> clearMainImage(@PathVariable Integer id) {
        Product p = productService.findById(id);
        if (p == null) return ResponseEntity.notFound().build();
        String oldImage = p.getHinhAnhChinh();
        p.setHinhAnhChinh(null);
        productRepository.save(p);
        fileUploadService.deleteAfterCommit(oldImage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/tu-dong-mo-ta")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE) or @sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public ResponseEntity<Map<String, String>> autoDescription(@RequestBody Map<String, String> params) {
        String description = autoDescriptionService.generate(params);
        return ResponseEntity.ok(Map.of("description", description));
    }

    private void addComboLists(Model model) {
        model.addAttribute("brands", productService.getDistinctThuongHieu());
        model.addAttribute("materials", productService.getDistinctChatLieu());
        model.addAttribute("origins", productService.getDistinctXuatXu());
        model.addAttribute("glassTypes", productService.getDistinctKinhLoai());
        model.addAttribute("purposes", productService.getDistinctMucDichSuDung());
    }

    private void notifyWishlistUsersOnVariantChanges(Product product,
            Map<Integer, Integer> oldStock,
            Map<Integer, BigDecimal> oldPrices) {
        List<ProductVariant> newVariants = productVariantRepository.findByProductIdAndIsActiveTrue(product.getId());

        boolean anyOutOfStock = newVariants.stream()
                .anyMatch(v -> oldStock.getOrDefault(v.getId(), 0) > 0
                        && (v.getSoLuongTon() == null || v.getSoLuongTon() <= 0));
        if (anyOutOfStock) {
            notificationHelper.notifyStaff(
                    "Sản phẩm " + product.getTenSanPham() + " vừa hết hàng!",
                    "PRODUCT", product.getId(),
                    "/admin/san-pham/sua/" + product.getId(),
                    "Xem sản phẩm"
            );
        }

        boolean backInStock = newVariants.stream()
                .anyMatch(v -> oldStock.getOrDefault(v.getId(), 0) <= 0
                && v.getSoLuongTon() != null && v.getSoLuongTon() > 0);
        Optional<BigDecimal> droppedPrice = newVariants.stream()
                .filter(v -> oldPrices.containsKey(v.getId()))
                .filter(v -> effectivePrice(v).compareTo(oldPrices.get(v.getId())) < 0)
                .map(this::effectivePrice)
                .min(BigDecimal::compareTo);
        if (!backInStock && droppedPrice.isEmpty()) {
            return;
        }

        List<Integer> userIds = wishlistRepository.findUserIdsByProductId(product.getId());
        for (Integer userId : userIds) {
            if (backInStock) {
                notificationHelper.notifyAll(
                        "Sản phẩm " + product.getTenSanPham() + " đã có hàng trở lại",
                        "PRODUCT", product.getId(),
                        "/san-pham/" + product.getId(),
                        "Xem ngay",
                        userId
                );
            }
            droppedPrice.ifPresent(price -> notificationHelper.notifyAll(
                    "Sản phẩm " + product.getTenSanPham() + " đã giảm giá còn " + formatCurrency(price),
                    "PRODUCT", product.getId(),
                    "/san-pham/" + product.getId(),
                    "Xem ngay",
                    userId
            ));
        }
    }

    private BigDecimal effectivePrice(ProductVariant variant) {
        return variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();
    }

    private String formatCurrency(BigDecimal price) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(price);
    }

    private String getCurrentAdminName() {
        try {
            User admin = securityUtil.getCurrentUser();
            return admin != null ? admin.getHoTen() : "";
        } catch (Exception e) {
            return "";
        }
    }

}
