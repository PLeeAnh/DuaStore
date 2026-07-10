package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.dto.VariantApiDTO;
import com.duastore.model.Category;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.service.PricingService;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.service.client.ProductService;
import com.duastore.service.client.ReviewService;
import com.duastore.service.client.WishlistService;
import com.duastore.service.FileUploadService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private static final int PAGE_SIZE = 24;

    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final FlashSaleRepository flashSaleRepository;
    private final PromotionRepository promotionRepository;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;
    private final FileUploadService fileUploadService;
    private final PricingService pricingService;
    private final OrderItemRepository orderItemRepository;

    private List<Category> buildCategoryBreadcrumb(Integer categoryId) {
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

    public ProductController(ProductService productService,
            ProductVariantRepository variantRepository,
            ProductImageRepository productImageRepository,
            CategoryRepository categoryRepository,
            ReviewService reviewService,
            FlashSaleRepository flashSaleRepository,
            PromotionRepository promotionRepository,
            WishlistService wishlistService,
            SecurityUtil securityUtil,
            FileUploadService fileUploadService,
            PricingService pricingService,
            OrderItemRepository orderItemRepository) {
        this.productService = productService;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.flashSaleRepository = flashSaleRepository;
        this.promotionRepository = promotionRepository;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
        this.fileUploadService = fileUploadService;
        this.pricingService = pricingService;
        this.orderItemRepository = orderItemRepository;
    }

    @GetMapping("/san-pham")
    public String list(@RequestParam(required = false) Integer danhMuc,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer dungTich,
            @RequestParam(required = false) String chatLieu,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("title", "san-pham");

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }
        if (chatLieu != null && chatLieu.isBlank()) {
            chatLieu = null;
        }
        if (priceRange != null && priceRange.isBlank()) {
            priceRange = null;
        }

        boolean hasFilters = (priceRange != null || dungTich != null || chatLieu != null
                || !"newest".equals(sortBy));

        Page<Product> productPage;
        if (hasFilters) {
            if (keyword != null) {
                model.addAttribute("keyword", keyword);
            }
            if (danhMuc != null) {
                categoryRepository.findById(danhMuc).ifPresent(c -> {
                    model.addAttribute("selectedCategory", c);
                    model.addAttribute("categoryBreadcrumb", buildCategoryBreadcrumb(danhMuc));
                });
            }
            productPage = productService.filterPaged(keyword, danhMuc, chatLieu, priceRange, dungTich, sortBy, page, PAGE_SIZE);
        } else if (keyword != null && !keyword.isBlank()) {
            productPage = productService.searchPaged(keyword, page, PAGE_SIZE);
            model.addAttribute("keyword", keyword);
        } else if (danhMuc != null) {
            List<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(danhMuc);
            categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(danhMuc)
                    .forEach(child -> categoryIds.add(child.getId()));
            productPage = productService.findByCategoriesPaged(categoryIds, page, PAGE_SIZE);
            categoryRepository.findById(danhMuc).ifPresent(c -> {
                model.addAttribute("selectedCategory", c);
                model.addAttribute("categoryBreadcrumb", buildCategoryBreadcrumb(danhMuc));
            });
        } else {
            productPage = productService.getDangBanPaged(page, PAGE_SIZE);
        }
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("categories", categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc());
        model.addAttribute("selectedCategoryId", danhMuc);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("dungTich", dungTich);
        model.addAttribute("chatLieu", chatLieu);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("distinctVolumes", productService.getDistinctVolumes());
        model.addAttribute("distinctMaterials", productService.getDistinctChatLieu());

        // Pagination attributes
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", (int) productPage.getTotalElements());
        model.addAttribute("pageSize", PAGE_SIZE);

        // Build variants map + flash sale map
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
        List<Product> products = productPage.getContent();
        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> allVariants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
            variantsMap = allVariants.stream()
                    .collect(Collectors.groupingBy(ProductVariant::getProductId));
            flashSaleMap.putAll(pricingService.loadActiveFlashSaleMap(ids));
        }
        model.addAttribute("variantsMap", variantsMap);
        model.addAttribute("flashSaleMap", flashSaleMap);

        // Active promotions for product cards
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActiveNow(now);
        model.addAttribute("activePromotions", activePromotions);
        BigDecimal maxPct = new BigDecimal("100");
        Promotion bestPercentagePromo = activePromotions.stream()
                .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                .filter(p -> p.getGiaTriGiam().compareTo(maxPct) <= 0)
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(Promotion::getGiaTriGiam))
                .orElse(null);
        Promotion bestFixedPromo = activePromotions.stream()
                .filter(p -> "SO_TIEN".equals(p.getLoaiGiam()))
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(Promotion::getGiaTriGiam))
                .orElse(null);
        model.addAttribute("bestPercentagePromo", bestPercentagePromo);
        model.addAttribute("bestFixedPromo", bestFixedPromo);

        // Pre‑compute promo discounted price for the first variant per product (for button text)
        Map<Integer, BigDecimal> promoPriceMap = new HashMap<>();
        Map<Integer, BigDecimal> variantPromoPriceMap = new HashMap<>();
        if (bestPercentagePromo != null) {
            BigDecimal discountPct = bestPercentagePromo.getGiaTriGiam();
            boolean hasGiamToiDa = bestPercentagePromo.getGiamToiDa() != null;
            for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
                Integer productId = entry.getKey();
                List<ProductVariant> pvList = entry.getValue();
                for (ProductVariant pv : pvList) {
                    BigDecimal basePrice = pv.getGiaKhuyenMai() != null ? pv.getGiaKhuyenMai() : pv.getGiaGoc();
                    if (basePrice != null) {
                        BigDecimal raw = basePrice
                                .multiply(BigDecimal.valueOf(100).subtract(discountPct))
                                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        if (hasGiamToiDa) {
                            BigDecimal actualDiscount = basePrice.multiply(discountPct)
                                    .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                            if (actualDiscount.compareTo(bestPercentagePromo.getGiamToiDa()) > 0) {
                                raw = basePrice.subtract(bestPercentagePromo.getGiamToiDa());
                            }
                        }
                        variantPromoPriceMap.put(pv.getId(), raw.setScale(0, java.math.RoundingMode.HALF_UP));
                    }
                }
                if (!pvList.isEmpty()) {
                    ProductVariant first = pvList.get(0);
                    BigDecimal basePrice = first.getGiaKhuyenMai() != null ? first.getGiaKhuyenMai() : first.getGiaGoc();
                    if (basePrice != null) {
                        BigDecimal raw = basePrice
                                .multiply(BigDecimal.valueOf(100).subtract(discountPct))
                                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        if (hasGiamToiDa) {
                            BigDecimal actualDiscount = basePrice.multiply(discountPct)
                                    .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                            if (actualDiscount.compareTo(bestPercentagePromo.getGiamToiDa()) > 0) {
                                raw = basePrice.subtract(bestPercentagePromo.getGiamToiDa());
                            }
                        }
                        promoPriceMap.put(productId, raw.setScale(0, java.math.RoundingMode.HALF_UP));
                    }
                }
            }
        }
        model.addAttribute("promoPriceMap", promoPriceMap);
        model.addAttribute("variantPromoPriceMap", variantPromoPriceMap);

        // Group variants by cap type for card display
        Map<Integer, Map<String, List<ProductVariant>>> groupedVariantsMap = new HashMap<>();
        for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
            Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
            for (ProductVariant v : entry.getValue()) {
                String groupKey = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) {
                        groupKey = parts[1].trim();
                    }
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
    public String detail(@PathVariable Integer id,
            @RequestParam(defaultValue = "0") int reviewPage,
            @RequestParam(required = false) Integer reviewRating,
            Model model) {
        var product = productService.findById(id);
        if (product == null) {
            return "redirect:/san-pham?errorMsg=Khong+tim+thay+san+pham";
        }

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
                if (pi.getImageUrl() != null) {
                    galleryImages.add(pi.getImageUrl());
                }
            }
        } else {
            if (product.getHinhAnhChinh() != null) {
                galleryImages.add(product.getHinhAnhChinh());
            }
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
                if (parts.length >= 2) {
                    groupKey = parts[1].trim();
                }
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

        // ── Số liệu xã hội thật: đã bán, lượt yêu thích, đánh giá trung bình ──
        model.addAttribute("soldCount", orderItemRepository.sumSoldQuantityByProductId(id));
        model.addAttribute("wishlistCount", wishlistService.countByProduct(id));
        model.addAttribute("ratingSummary", reviewService.getRatingSummary(id));

        // Active promotions for discount badge
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActiveNow(now);
        BigDecimal maxPct = new BigDecimal("100");
        Promotion bestPercentagePromo = activePromotions.stream()
                .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                .filter(p -> p.getGiaTriGiam().compareTo(maxPct) <= 0)
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(Promotion::getGiaTriGiam))
                .orElse(null);
        Promotion bestFixedPromo = activePromotions.stream()
                .filter(p -> "SO_TIEN".equals(p.getLoaiGiam()))
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(Promotion::getGiaTriGiam))
                .orElse(null);
        model.addAttribute("bestPercentagePromo", bestPercentagePromo);
        model.addAttribute("bestFixedPromo", bestFixedPromo);
        model.addAttribute("productPromotions", activePromotions);

        BigDecimal promoDiscountedPrice = null;
        Map<Integer, BigDecimal> variantPromoPriceMap = new HashMap<>();
        if (bestPercentagePromo != null && !variants.isEmpty()) {
            BigDecimal discountPct = bestPercentagePromo.getGiaTriGiam();
            boolean hasGiamToiDa = bestPercentagePromo.getGiamToiDa() != null;
            for (ProductVariant pv : variants) {
                BigDecimal basePrice = pv.getGiaKhuyenMai() != null ? pv.getGiaKhuyenMai() : pv.getGiaGoc();
                if (basePrice != null) {
                    BigDecimal raw = basePrice
                            .multiply(BigDecimal.valueOf(100).subtract(discountPct))
                            .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                    if (hasGiamToiDa) {
                        BigDecimal maxDiscount = bestPercentagePromo.getGiamToiDa();
                        BigDecimal actualDiscount = basePrice.multiply(discountPct)
                                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        if (actualDiscount.compareTo(maxDiscount) > 0) {
                            raw = basePrice.subtract(maxDiscount);
                        }
                    }
                    variantPromoPriceMap.put(pv.getId(), raw.setScale(0, java.math.RoundingMode.HALF_UP));
                }
            }
            if (!variants.isEmpty()) {
                ProductVariant first = variants.get(0);
                BigDecimal basePrice = first.getGiaKhuyenMai() != null ? first.getGiaKhuyenMai() : first.getGiaGoc();
                if (basePrice != null) {
                    promoDiscountedPrice = variantPromoPriceMap.get(first.getId());
                }
            }
        }
        model.addAttribute("promoDiscountedPrice", promoDiscountedPrice);
        model.addAttribute("variantPromoPriceMap", variantPromoPriceMap);

        int reviewSize = 10;
        Integer currentUserId = null;
        try {
            currentUserId = securityUtil.getCurrentUserId();
        } catch (Exception e) {
        }
        var reviewPageResult = reviewService.getApprovedReviews(id, reviewPage, reviewSize, currentUserId, reviewRating);
        model.addAttribute("reviews", reviewPageResult.getContent());
        model.addAttribute("reviewCurrentPage", reviewPage);
        model.addAttribute("reviewTotalPages", reviewPageResult.getTotalPages());
        model.addAttribute("reviewTotalItems", reviewPageResult.getTotalElements());
        model.addAttribute("reviewRating", reviewRating);
        model.addAttribute("ratingDistribution", reviewService.getRatingDistribution(id));
        if (currentUserId != null) {
            try {
                model.addAttribute("hasReviewed", reviewService.hasReviewed(currentUserId, id));
                model.addAttribute("canReview", reviewService.hasCompletedOrderAndPurchased(currentUserId, id) && !reviewService.hasReviewed(currentUserId, id));
            } catch (Exception e) {
                model.addAttribute("hasReviewed", false);
                model.addAttribute("canReview", false);
            }
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("canReview", false);
        }
        model.addAttribute("reviewRequest", new com.duastore.dto.ReviewRequestDTO());
        model.addAttribute("productId", id);

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

        // Compute initial promo discounted price for server-rendered display
        if (bestPercentagePromo != null && !variantPromoPriceMap.isEmpty() && minPrice != null) {
            BigDecimal promoMin = variantPromoPriceMap.values().stream().min(BigDecimal::compareTo).orElse(null);
            if (promoMin != null) {
                model.addAttribute("promoDiscountedPrice", promoMin);
            }
        }

        // Category name + breadcrumb
        Integer catId = product.getDanhMucId();
        String categoryName = categoryRepository.findById(catId)
                .map(Category::getTenDanhMuc).orElse("—");
        model.addAttribute("categoryName", categoryName);
        if (catId != null) {
            model.addAttribute("categoryBreadcrumb", buildCategoryBreadcrumb(catId));
        }

        List<Product> related = productService.getRelatedProducts(id, product.getDanhMucId(), 8);
        model.addAttribute("relatedProducts", related);
        if (!related.isEmpty()) {
            List<Integer> relatedIds = related.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> relatedVariants = variantRepository.findByProductIdInAndIsActiveTrue(relatedIds);
            Map<Integer, List<ProductVariant>> relatedVariantsMap = relatedVariants.stream()
                    .collect(Collectors.groupingBy(ProductVariant::getProductId));
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

    @GetMapping("/san-pham/{id}/reviews")
    public String reviewsFragment(@PathVariable Integer id,
            @RequestParam(defaultValue = "0") int reviewPage,
            @RequestParam(required = false) Integer reviewRating,
            Model model) {
        int reviewSize = 10;
        Integer currentUserId = null;
        try {
            currentUserId = securityUtil.getCurrentUserId();
        } catch (Exception e) {
        }
        var reviewPageResult = reviewService.getApprovedReviews(id, reviewPage, reviewSize, currentUserId, reviewRating);
        model.addAttribute("productId", id);
        model.addAttribute("reviews", reviewPageResult.getContent());
        model.addAttribute("reviewCurrentPage", reviewPage);
        model.addAttribute("reviewTotalPages", reviewPageResult.getTotalPages());
        model.addAttribute("reviewTotalItems", reviewPageResult.getTotalElements());
        model.addAttribute("reviewRating", reviewRating);
        model.addAttribute("ratingDistribution", reviewService.getRatingDistribution(id));
        if (currentUserId != null) {
            try {
                model.addAttribute("hasReviewed", reviewService.hasReviewed(currentUserId, id));
                model.addAttribute("canReview", reviewService.hasCompletedOrderAndPurchased(currentUserId, id) && !reviewService.hasReviewed(currentUserId, id));
            } catch (Exception e) {
                model.addAttribute("hasReviewed", false);
                model.addAttribute("canReview", false);
            }
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("canReview", false);
        }
        model.addAttribute("reviewRequest", new ReviewRequestDTO());
        model.addAttribute("ratingSummary", reviewService.getRatingSummary(id));
        return "view/client/product/parts/product-reviews :: reviews";
    }

    @PostMapping("/san-pham/{id}/danh-gia")
    public Object submitReview(@PathVariable Integer id,
            @Valid @ModelAttribute ReviewRequestDTO request,
            BindingResult result,
            @RequestParam(value = "hinhAnh", required = false) List<MultipartFile> hinhAnhFiles,
            RedirectAttributes ra,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        boolean isAjax = "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"));
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            if (isAjax) return java.util.Map.of("success", false, "message", "Vui long dang nhap de danh gia");
            ra.addFlashAttribute("errorMsg", "Vui long dang nhap de danh gia");
            return "redirect:/dang-nhap";
        }
        if (result.hasErrors()) {
            if (isAjax) return java.util.Map.of("success", false, "message", "Vui long nhap day du thong tin danh gia");
            ra.addFlashAttribute("errorMsg", "Vui long nhap day du thong tin danh gia");
            return "redirect:/san-pham/" + id;
        }
        request.setProductId(id);
        String hinhAnhUrls = null;
        if (hinhAnhFiles != null && !hinhAnhFiles.isEmpty()) {
            try {
                List<String> urls = new java.util.ArrayList<>();
                for (MultipartFile f : hinhAnhFiles) {
                    if (!f.isEmpty()) {
                        urls.add(fileUploadService.save(f, "reviews"));
                    }
                }
                if (!urls.isEmpty()) {
                    hinhAnhUrls = String.join(",", urls);
                }
            } catch (Exception e) {
                if (isAjax) return java.util.Map.of("success", false, "message", "Loi upload anh: " + e.getMessage());
                ra.addFlashAttribute("errorMsg", "Loi upload anh: " + e.getMessage());
                return "redirect:/san-pham/" + id;
            }
        }
        try {
            reviewService.createReview(userId, request, hinhAnhUrls);
            if (isAjax) return java.util.Map.of("success", true, "message", "Cam on ban da danh gia!");
            ra.addFlashAttribute("successMsg", "Cam on ban da danh gia!");
        } catch (Exception e) {
            if (isAjax) return java.util.Map.of("success", false, "message", e.getMessage());
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

    @GetMapping("/api/products/suggestions")
    @ResponseBody
    public List<Map<String, Object>> suggest(@RequestParam String keyword) {
        List<Product> products = productService.searchSuggestions(keyword, 5);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Product p : products) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", p.getId());
            m.put("tenSanPham", p.getTenSanPham());
            m.put("hinhAnhChinh", p.getHinhAnhChinh());
            result.add(m);
        }
        return result;
    }
}
