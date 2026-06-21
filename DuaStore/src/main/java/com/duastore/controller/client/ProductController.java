package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.dto.VariantApiDTO;
import com.duastore.model.Category;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.ProductService;
import com.duastore.service.client.ReviewService;
import com.duastore.service.client.WishlistService;
import com.duastore.service.FileUploadService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final FlashSaleRepository flashSaleRepository;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;
    private final FileUploadService fileUploadService;

    public ProductController(ProductService productService,
                             ProductVariantRepository variantRepository,
                             ProductImageRepository productImageRepository,
                             CategoryRepository categoryRepository,
                             ReviewService reviewService,
                             FlashSaleRepository flashSaleRepository,
                             WishlistService wishlistService,
                             SecurityUtil securityUtil,
                             FileUploadService fileUploadService) {
        this.productService = productService;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.flashSaleRepository = flashSaleRepository;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/san-pham")
    public String list(@RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) BigDecimal minPrice,
                       @RequestParam(required = false) BigDecimal maxPrice,
                       @RequestParam(required = false) Integer dungTich,
                       @RequestParam(required = false) String kieuNap,
                       @RequestParam(required = false) String hinhDang,
                       @RequestParam(defaultValue = "newest") String sortBy,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        model.addAttribute("title", "san-pham");

        boolean hasFilters = (minPrice != null || maxPrice != null || dungTich != null || kieuNap != null || hinhDang != null
                || !"newest".equals(sortBy));

        Page<Product> productPage;
        if (hasFilters) {
            if (keyword != null && keyword.isBlank()) keyword = null;
            if (keyword != null) model.addAttribute("keyword", keyword);
            if (danhMuc != null) {
                categoryRepository.findById(danhMuc).ifPresent(c -> model.addAttribute("selectedCategory", c));
            }
            productPage = productService.filterPaged(keyword, danhMuc, minPrice, maxPrice, dungTich, kieuNap, hinhDang, sortBy, page, size);
        } else if (keyword != null && !keyword.isBlank()) {
            productPage = productService.searchPaged(keyword, page, size);
            model.addAttribute("keyword", keyword);
        } else if (danhMuc != null) {
            List<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(danhMuc);
            categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(danhMuc)
                    .forEach(child -> categoryIds.add(child.getId()));
            productPage = productService.findByCategoriesPaged(categoryIds, page, size);
            categoryRepository.findById(danhMuc).ifPresent(c -> model.addAttribute("selectedCategory", c));
        } else {
            productPage = productService.getDangBanPaged(page, size);
        }
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc());
        model.addAttribute("selectedCategoryId", danhMuc);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("dungTich", dungTich);
        model.addAttribute("kieuNap", kieuNap);
        model.addAttribute("hinhDang", hinhDang);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("distinctVolumes", productService.getDistinctVolumes());
        model.addAttribute("distinctCapTypes", productService.getDistinctCapTypes());
        model.addAttribute("distinctShapes", productService.getDistinctShapes());

        // Pagination attributes
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", (int) productPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Build variants map + flash sale map
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
        List<Product> products = productPage.getContent();
        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> allVariants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
            variantsMap = allVariants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));
            List<FlashSale> activeFlashSales = flashSaleRepository.findActiveNow(LocalDateTime.now());
            for (FlashSale fs : activeFlashSales) {
                flashSaleMap.put(fs.getProductId(), fs);
            }
        }
        model.addAttribute("variantsMap", variantsMap);
        model.addAttribute("flashSaleMap", flashSaleMap);

        // Group variants by cap type for card display
        Map<Integer, Map<String, List<ProductVariant>>> groupedVariantsMap = new HashMap<>();
        for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
            Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
            for (ProductVariant v : entry.getValue()) {
                String groupKey = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) groupKey = parts[1].trim();
                } else if (v.getDungTich() != null) {
                    groupKey = "Dung tích";
                }
                grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(v);
            }
            groupedVariantsMap.put(entry.getKey(), grouped);
        }
        model.addAttribute("groupedVariantsMap", groupedVariantsMap);

        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            model.addAttribute("avgRatings", reviewService.getAverageRatings(ids));
        }

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                model.addAttribute("likedIds", wishlistService.getLikedProductIds(userId));
            }
        } catch (Exception e) {
            log.warn("Loi doc likedIds o trang danh sach san pham: {}", e.getMessage());
        }

        return "view/client/product/product-list";
    }

    @GetMapping("/san-pham/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var product = productService.findById(id);
        if (product == null) return "redirect:/san-pham?errorMsg=Khong+tim+thay+san+pham";

        List<ProductVariant> variants = productService.getVariants(id);
        model.addAttribute("title", product.getTenSanPham());
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);

        // Build gallery images: ProductImages from DB + fallback to main + variant images
        List<ProductImage> dbImages = productImageRepository
            .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id);
        List<String> galleryImages = new ArrayList<>();
        if (!dbImages.isEmpty()) {
            for (ProductImage pi : dbImages) {
                if (pi.getImageUrl() != null) galleryImages.add(pi.getImageUrl());
            }
        } else {
            if (product.getHinhAnhChinh() != null) galleryImages.add(product.getHinhAnhChinh());
            for (ProductVariant v : variants) {
                if (v.getHinhAnh() != null && !galleryImages.contains(v.getHinhAnh())) {
                    galleryImages.add(v.getHinhAnh());
                }
            }
        }
        model.addAttribute("galleryImages", galleryImages);

        // Group variants by cap type (parsed from tenBienThe, e.g. "50ml - Nắp Gỗ")
        Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String groupKey = "Khác";
            if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                if (parts.length >= 2) groupKey = parts[1].trim();
            }
            grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(v);
        }
        model.addAttribute("groupedVariants", grouped);

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                model.addAttribute("likedIds", wishlistService.getLikedProductIds(userId));
            }
        } catch (Exception e) {
            log.warn("Loi doc likedIds o trang chi tiet san pham: {}", e.getMessage());
        }

        // Price range
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        Integer minVolume = null;
        Integer maxVolume = null;
        if (!variants.isEmpty()) {
            List<BigDecimal> prices = variants.stream()
                .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
                .sorted().collect(Collectors.toList());
            minPrice = prices.get(0);
            maxPrice = prices.get(prices.size() - 1);
            List<Integer> volumes = variants.stream()
                .map(ProductVariant::getDungTich)
                .filter(Objects::nonNull)
                .sorted().collect(Collectors.toList());
            if (!volumes.isEmpty()) {
                minVolume = volumes.get(0);
                maxVolume = volumes.get(volumes.size() - 1);
            }
        }
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minVolume", minVolume);
        model.addAttribute("maxVolume", maxVolume);

        // Category name
        String categoryName = categoryRepository.findById(product.getDanhMucId())
            .map(Category::getTenDanhMuc).orElse("—");
        model.addAttribute("categoryName", categoryName);

        List<Product> related = productService.getRelatedProducts(id, product.getDanhMucId(), 8);
        model.addAttribute("relatedProducts", related);
        if (!related.isEmpty()) {
            List<Integer> relatedIds = related.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> relatedVariants = variantRepository.findByProductIdInAndIsActiveTrue(relatedIds);
            Map<Integer, List<ProductVariant>> relatedVariantsMap = relatedVariants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));
            // Compute min prices for related products
            Map<Integer, BigDecimal> relatedMinPrices = new HashMap<>();
            for (var entry : relatedVariantsMap.entrySet()) {
                entry.getValue().stream()
                    .map(v -> v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc())
                    .min(BigDecimal::compareTo)
                    .ifPresent(price -> relatedMinPrices.put(entry.getKey(), price));
            }
            model.addAttribute("relatedMinPrices", relatedMinPrices);
        }

        return "view/client/product/product-detail";
    }

    @PostMapping("/san-pham/{id}/danh-gia")
    public String submitReview(@PathVariable Integer id,
                               @Valid @ModelAttribute ReviewRequestDTO request,
                               BindingResult result,
                               @RequestParam(value = "hinhAnh", required = false) MultipartFile hinhAnhFile,
                               RedirectAttributes ra) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            ra.addFlashAttribute("errorMsg", "Vui long dang nhap de danh gia");
            return "redirect:/dang-nhap";
        }
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Vui long nhap day du thong tin danh gia");
            return "redirect:/san-pham/" + id;
        }
        request.setProductId(id);
        String hinhAnhUrl = null;
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            try {
                hinhAnhUrl = fileUploadService.save(hinhAnhFile);
            } catch (Exception e) {
                ra.addFlashAttribute("errorMsg", "Loi upload anh: " + e.getMessage());
                return "redirect:/san-pham/" + id;
            }
        }
        try {
            reviewService.createReview(userId, request, hinhAnhUrl);
            ra.addFlashAttribute("successMsg", "Cam on ban da danh gia! Danh gia se duoc hien thi sau khi duyet.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/san-pham/" + id;
    }

    // ── API for variant switching (AJAX) ──

    @GetMapping("/api/variants/{variantId}")
    @ResponseBody
    public VariantApiDTO getVariant(@PathVariable Integer variantId) {
        return productService.getVariantApi(variantId);
    }
}