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
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final FlashSaleRepository flashSaleRepository;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityUtil securityUtil;

    public ProductController(ProductService productService,
                             ProductVariantRepository variantRepository,
                             ProductImageRepository productImageRepository,
                             CategoryRepository categoryRepository,
                             ReviewService reviewService,
                             FlashSaleRepository flashSaleRepository,
                             JdbcTemplate jdbcTemplate,
                             SecurityUtil securityUtil) {
        this.productService = productService;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.flashSaleRepository = flashSaleRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/san-pham")
    public String list(@RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("title", "san-pham");

        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.search(keyword);
            model.addAttribute("keyword", keyword);
        } else if (danhMuc != null) {
            List<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(danhMuc);
            categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(danhMuc)
                    .forEach(child -> categoryIds.add(child.getId()));
            products = productService.findByCategories(categoryIds);
            categoryRepository.findById(danhMuc).ifPresent(c -> model.addAttribute("selectedCategory", c));
        } else {
            products = productService.getDangBan();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc());
        model.addAttribute("selectedCategoryId", danhMuc);

        // Build variants map + flash sale map
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
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
                String capType = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) capType = parts[1].trim();
                } else if (v.getDungTich() != null) {
                    capType = "Dung tích";
                }
                grouped.computeIfAbsent(capType, k -> new ArrayList<>()).add(v);
            }
            groupedVariantsMap.put(entry.getKey(), grouped);
        }
        model.addAttribute("groupedVariantsMap", groupedVariantsMap);

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
                model.addAttribute("likedIds", likedIds);
            }
        } catch (Exception e) {
            System.out.println("Loi doc likedIds o trang danh sach san pham: " + e.getMessage());
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
            String capType = "Khác";
            if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                if (parts.length >= 2) capType = parts[1].trim();
            }
            grouped.computeIfAbsent(capType, k -> new ArrayList<>()).add(v);
        }
        model.addAttribute("groupedVariants", grouped);

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
                model.addAttribute("likedIds", likedIds);
            }
        } catch (Exception e) {
            System.out.println("Loi doc likedIds o trang chi tiet san pham: " + e.getMessage());
        }

        model.addAttribute("reviews", reviewService.getApprovedReviews(id));
        Integer currentUserId = securityUtil.getCurrentUserId();
        if (currentUserId != null) {
            try {
                model.addAttribute("hasReviewed", reviewService.hasReviewed(currentUserId, id));
            } catch (Exception e) {
                model.addAttribute("hasReviewed", false);
            }
        } else {
            model.addAttribute("hasReviewed", false);
        }

        return "view/client/product/product-detail";
    }

    @PostMapping("/san-pham/{id}/danh-gia")
    public String submitReview(@PathVariable Integer id,
                               @Valid @ModelAttribute ReviewRequestDTO request,
                               BindingResult result,
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
        try {
            reviewService.createReview(userId, request);
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