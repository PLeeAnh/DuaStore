package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Category;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserVoucherRepository;
import com.duastore.repository.WishlistRepository;
import com.duastore.model.VoucherStatus;
import com.duastore.service.BannerService;
import com.duastore.service.PricingService;
import com.duastore.service.SiteSettingService;
import com.duastore.service.client.CategoryService;
import com.duastore.service.client.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductVariantRepository variantRepository;
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BannerService bannerService;
    private final UserVoucherRepository userVoucherRepository;
    private final SecurityUtil securityUtil;
    private final SiteSettingService siteSettingService;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ReviewsRepository reviewsRepository;
    private final PricingService pricingService;
    private final ProductImageRepository productImageRepository;

    public HomeController(ProductService productService,
            CategoryService categoryService,
            FlashSaleRepository flashSaleRepository,
            ProductVariantRepository variantRepository,
            PromotionRepository promotionRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BannerService bannerService,
            UserVoucherRepository userVoucherRepository,
            SecurityUtil securityUtil,
            SiteSettingService siteSettingService,
            OrderItemRepository orderItemRepository,
            WishlistRepository wishlistRepository,
            ReviewsRepository reviewsRepository,
            PricingService pricingService,
            ProductImageRepository productImageRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.flashSaleRepository = flashSaleRepository;
        this.variantRepository = variantRepository;
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.bannerService = bannerService;
        this.userVoucherRepository = userVoucherRepository;
        this.securityUtil = securityUtil;
        this.siteSettingService = siteSettingService;
        this.orderItemRepository = orderItemRepository;
        this.wishlistRepository = wishlistRepository;
        this.reviewsRepository = reviewsRepository;
        this.pricingService = pricingService;
        this.productImageRepository = productImageRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");

        // Read homepage section settings
        int promoLimit = parseInt(siteSettingService.getValue("hp_3_limit"), 6);
        int catLimit = parseInt(siteSettingService.getValue("hp_4_limit"), 7);
        int prodLimit = parseInt(siteSettingService.getValue("hp_5_limit"), 8);
        int promoLayout = parseInt(siteSettingService.getValue("hp_3_layout"), 3);
        int catLayout = parseInt(siteSettingService.getValue("hp_4_layout"), 4);
        int prodLayout = parseInt(siteSettingService.getValue("hp_5_layout"), 4);

        // Grid class for promotions (Bootstrap responsive)
        int clampedLayout = Math.min(Math.max(promoLayout, 1), 6);
        String mdClass = switch (clampedLayout) {
            case 1 ->
                "col-md-12";
            case 2 ->
                "col-md-6";
            case 3 ->
                "col-md-4";
            case 4 ->
                "col-md-3";
            default ->
                "col-md";
        };
        model.addAttribute("promotionGridClass", "col-6 " + mdClass);

        // Column count for categories & products (CSS Grid --cols)
        model.addAttribute("categoryColumns", catLayout);
        model.addAttribute("productColumns", prodLayout);

        // Featured promotions with limit
        List<Promotion> featuredPromos = promotionRepository.findFeaturedPromotions(LocalDateTime.now(),
                PageRequest.of(0, promoLimit));
        model.addAttribute("featuredPromotions", featuredPromos);

        // Available voucher count for logged-in user
        Integer currentUserId = null;
        try {
            currentUserId = securityUtil.getCurrentUserId();
        } catch (Exception ignored) {
        }
        if (currentUserId != null) {
            model.addAttribute("voucherCount", userVoucherRepository.countByUserIdAndStatus(currentUserId, VoucherStatus.AVAILABLE));
        }

        // Active banners for hero section
        model.addAttribute("banners", bannerService.getActiveForClient());

        List<Product> featured = productService.getFeatured();
        model.addAttribute("featuredProducts", featured.stream().limit(prodLimit).toList());
        model.addAttribute("featuredCategories", categoryService.getFeaturedCategories().stream().limit(catLimit).toList());

        // ─ Bán chạy nhất (Top sellers) - chỉ tính đơn đã giao/hoàn thành ─
        List<Object[]> topSellingRows = orderItemRepository.findTopSellingProductIds(PageRequest.of(0, 6));
        List<Integer> topSellerIds = topSellingRows.stream().map(r -> (Integer) r[0]).toList();
        Map<Integer, Long> soldCountMap = new HashMap<>();
        for (Object[] row : topSellingRows) {
            soldCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
        }
        Map<Integer, Product> productById = productRepository.findAllById(topSellerIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> topSellers = topSellerIds.stream()
                .map(productById::get)
                .filter(p -> p != null && p.isActive())
                .toList();
        model.addAttribute("topSellers", topSellers);
        model.addAttribute("soldCountMap", soldCountMap);

        // ─ Sản phẩm mới ─
        List<Product> newestProducts = productRepository.findByIsActiveTrueOrderByNgayTaoDesc(PageRequest.of(0, 10)).getContent();
        model.addAttribute("newestProducts", newestProducts);

        // ─ Dưới 300.000đ ─
        List<Product> underPriceProducts = productRepository.findUnderPrice(new BigDecimal("300000"), PageRequest.of(0, 10));
        model.addAttribute("underPriceProducts", underPriceProducts);

        // ─ Duyệt sản phẩm (Browse) - sản phẩm nổi bật cho section hai cột ─
        List<Product> browseProducts = featured.stream().limit(10).toList();
        model.addAttribute("browseProducts", browseProducts);

        // Tính rating trung bình + số lượt yêu thích cho browse products
        Map<Integer, Double> ratingMap = new HashMap<>();
        Map<Integer, Long> ratingCountMap = new HashMap<>();
        Map<Integer, Long> wishlistCountMap = new HashMap<>();
        if (!browseProducts.isEmpty()) {
            List<Integer> browseIds = browseProducts.stream().map(Product::getId).toList();
            List<Object[]> ratingRows = reviewsRepository.getAverageRatings(browseIds);
            for (Object[] row : ratingRows) {
                Integer pid = (Integer) row[0];
                Double avg = row[1] != null ? ((Number) row[1]).doubleValue() : null;
                Long cnt = row[2] != null ? ((Number) row[2]).longValue() : 0L;
                ratingMap.put(pid, avg);
                ratingCountMap.put(pid, cnt);
            }
            List<Object[]> wlRows = wishlistRepository.countByProductIds(browseIds);
            for (Object[] row : wlRows) {
                wishlistCountMap.put((Integer) row[0], ((Number) row[1]).longValue());
            }
        }
        model.addAttribute("ratingMap", ratingMap);
        model.addAttribute("ratingCountMap", ratingCountMap);
        model.addAttribute("wishlistCountMap", wishlistCountMap);

        // Gallery images cho browse section
        Map<Integer, List<String>> browseGalleryMap = new HashMap<>();
        if (!browseProducts.isEmpty()) {
            List<Integer> browseIds = browseProducts.stream().map(Product::getId).toList();
            Map<Integer, String> mainImgMap = browseProducts.stream()
                    .filter(p -> p.getHinhAnhChinh() != null)
                    .collect(Collectors.toMap(Product::getId, Product::getHinhAnhChinh));
            Map<Integer, List<com.duastore.model.ProductImage>> grouped = productImageRepository
                    .findByProductIdInAndIsActiveTrue(browseIds)
                    .stream()
                    .collect(Collectors.groupingBy(com.duastore.model.ProductImage::getProductId));
            for (Integer pid : browseIds) {
                List<String> urls = new ArrayList<>();
                if (mainImgMap.containsKey(pid)) {
                    urls.add(mainImgMap.get(pid));
                }
                List<com.duastore.model.ProductImage> imgs = grouped.getOrDefault(pid, java.util.Collections.emptyList());
                int maxBrowseImgs = 10;
                for (com.duastore.model.ProductImage img : imgs) {
                    if (img.getImageUrl() != null && !urls.contains(img.getImageUrl())) {
                        if (urls.size() >= maxBrowseImgs) break;
                        urls.add(img.getImageUrl());
                    }
                }
                browseGalleryMap.put(pid, urls);
            }
        }
        model.addAttribute("browseGalleryMap", browseGalleryMap);

        Map<Integer, Long> productCountMap = productRepository.countProductsByDanhMuc()
                .stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));

        // Aggregate child category counts into parents
        List<Category> allCategories = categoryRepository.findByIsActiveTrue();
        for (Category cat : allCategories) {
            if (cat.getParent() != null) {
                Integer parentId = cat.getParent().getId();
                Long childCount = productCountMap.getOrDefault(cat.getId(), 0L);
                productCountMap.merge(parentId, childCount, Long::sum);
            }
        }
        model.addAttribute("productCountMap", productCountMap);
        long totalProducts = productCountMap.values().stream().mapToLong(Long::longValue).sum();
        model.addAttribute("totalProducts", totalProducts);

        // Map danh mục ID → tên danh mục (dùng cho browse section)
        Map<Integer, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getTenDanhMuc, (a, b) -> a));
        model.addAttribute("categoryNameMap", categoryNameMap);

        // Gộp toàn bộ ID sản phẩm ở mọi khối để tính chung 1 lần variants/flashsale/promo
        List<Product> allSectionProducts = new ArrayList<>();
        allSectionProducts.addAll(featured);
        allSectionProducts.addAll(topSellers);
        allSectionProducts.addAll(newestProducts);
        allSectionProducts.addAll(underPriceProducts);
        allSectionProducts.addAll(browseProducts);

        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        if (!allSectionProducts.isEmpty()) {
            List<Integer> ids = allSectionProducts.stream().map(Product::getId).distinct().collect(Collectors.toList());
            flashSaleMap = pricingService.loadActiveFlashSaleMap(ids);
            List<ProductVariant> allVariants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
            variantsMap = allVariants.stream()
                    .collect(Collectors.groupingBy(ProductVariant::getProductId));
        }
        model.addAttribute("flashSaleMap", flashSaleMap);
        model.addAttribute("variantsMap", variantsMap);

        // Group variants by cap type for card display
        Map<Integer, Map<String, List<ProductVariant>>> groupedVariantsMap = new HashMap<>();
        for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
            Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
            for (ProductVariant v : entry.getValue()) {
                String kieuNap = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) {
                        kieuNap = parts[1].trim();
                    }
                } else if (v.getDungTich() != null) {
                    kieuNap = "Dung tích";
                }
                grouped.computeIfAbsent(kieuNap, k -> new ArrayList<>()).add(v);
            }
            groupedVariantsMap.put(entry.getKey(), grouped);
        }
        model.addAttribute("groupedVariantsMap", groupedVariantsMap);

        // Active promotions for homepage
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

        return "view/client/index";
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
