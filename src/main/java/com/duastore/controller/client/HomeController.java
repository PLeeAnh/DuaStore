package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserVoucherRepository;
import com.duastore.repository.WishlistRepository;
import com.duastore.model.VoucherStatus;
import com.duastore.service.BannerService;
import com.duastore.model.FlashSaleItem;
import com.duastore.service.PricingService;
import com.duastore.service.PricingService.FlashSaleOffer;
import com.duastore.service.SiteSettingService;
import com.duastore.service.client.CategoryService;
import com.duastore.service.client.ProductService;
import com.duastore.repository.ReviewsRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
/**
 * Controller xử lý các request HTTP liên quan tới home controller.
 */
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
    private final PricingService pricingService;
    private final ReviewsRepository reviewsRepository;

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
            PricingService pricingService,
            ReviewsRepository reviewsRepository) {
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
        this.pricingService = pricingService;
        this.reviewsRepository = reviewsRepository;
    }

    /**
     * Check if a promotion applies to a given product based on targetType and targetIds.
     * targetType: ALL (or null) -> applies to all products
     * targetType: CATEGORY -> product's category (or parent) must be in targetIds
     * targetType: PRODUCT -> product's id must be in targetIds
     * targetType: USER_GROUP -> requires user context, default to true for now
     */
    private boolean isPromotionApplicableToProduct(Promotion promo, Product product) {
        if (promo == null || product == null) {
            return false;
        }
        String targetType = promo.getTargetType();
        String targetIds = promo.getTargetIds();

        if (targetType == null || targetType.isBlank() || "ALL".equalsIgnoreCase(targetType)) {
            return true;
        }
        if (targetIds == null || targetIds.isBlank()) {
            return false;
        }

        Set<String> targetIdSet = Arrays.stream(targetIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (targetIdSet.isEmpty()) {
            return false;
        }

        switch (targetType.toUpperCase()) {
            case "CATEGORY":
                Integer catId = product.getDanhMucId();
                if (catId != null && targetIdSet.contains(catId.toString())) {
                    return true;
                }
                // Check parent category
                if (catId != null) {
                    Category cat = categoryRepository.findById(catId).orElse(null);
                    if (cat != null && cat.getParent() != null && targetIdSet.contains(cat.getParent().getId().toString())) {
                        return true;
                    }
                }
                return false;
            case "PRODUCT":
                return targetIdSet.contains(product.getId().toString());
            case "USER_GROUP":
                // Requires user context, default to applicable
                return true;
            default:
                return false;
        }
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("title", "Trang chủ");

        Object loginErrorMessage = session.getAttribute("loginErrorMessage");
        if (loginErrorMessage != null) {
            model.addAttribute("loginErrorMessage", loginErrorMessage);
            session.removeAttribute("loginErrorMessage");
        }

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
        Integer currentUserId = securityUtil.getCurrentUserId();
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
        Map<Integer, Product> productById = productRepository.findAllByIdWithVariants(topSellerIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> topSellers = topSellerIds.stream()
                .map(productById::get)
                .filter(p -> p != null)
                .toList();
        model.addAttribute("topSellers", topSellers);
        model.addAttribute("soldCountMap", soldCountMap);

        // ─ Sản phẩm mới ─
        List<Product> newestProducts = productRepository.findNewestWithVariants(PageRequest.of(0, 10)).getContent();
        model.addAttribute("newestProducts", newestProducts);

        // ─ Dưới 300.000đ ─
        List<Product> underPriceProducts = productRepository.findUnderPrice(new BigDecimal("300000"), PageRequest.of(0, 10));
        model.addAttribute("underPriceProducts", underPriceProducts);

        // ─ Đang giảm giá (chiến dịch khuyến mãi) ─
        List<Product> discountedProducts = productRepository.findDiscountedWithVariants(PageRequest.of(0, 10));
        model.addAttribute("discountedProducts", discountedProducts);

        // ─ Yêu thích nhất (most wished) ─
        List<Object[]> mostWishedRows = wishlistRepository.findMostLiked(PageRequest.of(0, 8));
        List<Integer> mostWishedIds = mostWishedRows.stream().map(r -> ((Number) r[0]).intValue()).toList();
        Map<Integer, Long> wishCountMap = new HashMap<>();
        for (Object[] row : mostWishedRows) {
            wishCountMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        List<Product> mostWishedProducts = productRepository.findAllByIdWithVariants(mostWishedIds);
        model.addAttribute("mostWishedProducts", mostWishedProducts);
        model.addAttribute("wishCountMap", wishCountMap);

        // ─ Đánh giá nhiều nhất (most reviewed) ─
        List<Object[]> mostReviewedRows = reviewsRepository.findMostReviewed(PageRequest.of(0, 8));
        List<Integer> mostReviewedIds = mostReviewedRows.stream().map(r -> ((Number) r[0]).intValue()).toList();
        Map<Integer, Long> reviewCountMap = new HashMap<>();
        Map<Integer, Double> avgRatingMap = new HashMap<>();
        for (Object[] row : mostReviewedRows) {
            reviewCountMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            avgRatingMap.put(((Number) row[0]).intValue(), ((Number) row[2]) != null ? ((Number) row[2]).doubleValue() : 0.0);
        }
        List<Product> mostReviewedProducts = productRepository.findAllByIdWithVariants(mostReviewedIds);
        model.addAttribute("mostReviewedProducts", mostReviewedProducts);
        model.addAttribute("reviewCountMap", reviewCountMap);
        model.addAttribute("avgRatingMap", avgRatingMap);

        // Gộp toàn bộ ID sản phẩm ở mọi khối để tính chung 1 lần variants/flashsale/promo
        List<Product> allSectionProducts = new ArrayList<>();
        allSectionProducts.addAll(featured);
        allSectionProducts.addAll(topSellers);
        allSectionProducts.addAll(newestProducts);
        allSectionProducts.addAll(underPriceProducts);
        allSectionProducts.addAll(discountedProducts);
        allSectionProducts.addAll(mostWishedProducts);
        allSectionProducts.addAll(mostReviewedProducts);

// ── Các section động do admin thêm trong "Thiết kế trang chủ" ──
        List<Map<String, Object>> hpSections = new ArrayList<>();
        List<Product> hpDynamicProducts = new ArrayList<>();
        Map<String, String> hpSettingsMap = siteSettingService.getGroup("appearance");
        for (int i = 2; i <= 30; i++) {
            String hpType = hpSettingsMap.get("hp_" + i + "_type");
            if (hpType == null || hpType.isEmpty()) {
                break;
            }
            if ("0".equals(hpSettingsMap.get("hp_" + i + "_active"))) {
                continue;
            }
            String hpTitle = hpSettingsMap.get("hp_" + i + "_title");
            String hpStyle = hpSettingsMap.get("hp_" + i + "_layout_style");
            if (hpStyle == null || hpStyle.isEmpty()) {
                hpStyle = "grid";
            }
            boolean sliderStyle = "slider".equals(hpStyle);
            int hpCols = Math.min(Math.max(parseInt(hpSettingsMap.get("hp_" + i + "_layout"), 4), 2), 6);
            int hpLimit = Math.min(Math.max(parseInt(hpSettingsMap.get("hp_" + i + "_limit"), 8), 1), 24);

            if ("products".equals(hpType)) {
                String hpMode = hpSettingsMap.get("hp_" + i + "_mode");
                if (hpMode == null || hpMode.isEmpty()) {
                    hpMode = "featured";
                }
                int minP = parseInt(hpSettingsMap.get("hp_" + i + "_min_price"), 0);
                int maxP = parseInt(hpSettingsMap.get("hp_" + i + "_max_price"), 300000);
                List<Integer> hpCatIds = new ArrayList<>();
                String rawIds = hpSettingsMap.get("hp_" + i + "_category_ids");
                if (rawIds != null) {
                    for (String part : rawIds.split("[,;\\s]+")) {
                        try {
                            hpCatIds.add(Integer.parseInt(part.trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                List<Product> items = resolveHomepageProducts(hpMode, hpLimit, minP, maxP, hpCatIds);
                if (items.isEmpty()) {
                    continue;
                }
                hpDynamicProducts.addAll(items);
                Map<String, Object> sec = new LinkedHashMap<>();
                sec.put("type", "products");
                sec.put("title", hpTitle);
                sec.put("style", sliderStyle ? "slider" : "grid");
                sec.put("cols", hpCols);
                sec.put("items", items);
                sec.put("linkLabel", hpSettingsMap.get("hp_" + i + "_link_label"));
                sec.put("linkUrl", hpSettingsMap.get("hp_" + i + "_link_url"));
                hpSections.add(sec);
            } else if ("categories".equals(hpType)) {
                List<Category> hpCats = categoryService.getFeaturedCategories().stream().limit(hpLimit).toList();
                if (hpCats.isEmpty()) {
                    continue;
                }
                Map<String, Object> sec = new LinkedHashMap<>();
                sec.put("type", "categories");
                sec.put("title", hpTitle);
                sec.put("style", sliderStyle ? "slider" : "grid");
                sec.put("cols", hpCols);
                sec.put("items", hpCats);
                hpSections.add(sec);
            } else if ("promotions".equals(hpType)) {
                List<Promotion> hpPromos = promotionRepository.findActiveNow(LocalDateTime.now()).stream()
                        .limit(hpLimit).toList();
                if (hpPromos.isEmpty()) {
                    continue;
                }
                Map<String, Object> sec = new LinkedHashMap<>();
                sec.put("type", "promotions");
                sec.put("title", hpTitle);
                sec.put("style", sliderStyle ? "slider" : "grid");
                sec.put("cols", hpCols);
                sec.put("items", hpPromos);
                hpSections.add(sec);
            }
        }
        model.addAttribute("hpSections", hpSections);
        if (!hpDynamicProducts.isEmpty()) {
            allSectionProducts.addAll(hpDynamicProducts);
        }

        Map<Integer, FlashSaleOffer> flashSaleMap = new HashMap<>();
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        if (!allSectionProducts.isEmpty()) {
            List<Integer> ids = allSectionProducts.stream().map(Product::getId).distinct().collect(Collectors.toList());
            flashSaleMap = pricingService.loadActiveFlashSaleOffers(ids);
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

        // Active promotions for homepage - filter by targetType/targetIds per product
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> activePromotions = promotionRepository.findActiveNow(now);
        model.addAttribute("activePromotions", activePromotions);

        // ─ Chiến dịch khuyến mãi: nhóm sản phẩm theo từng chiến dịch (4 cột) ─
        // Chỉ các chiến dịch giảm giá sản phẩm (PHAN_TRAM) mới hiển thị sản phẩm;
        // voucher theo đơn hàng (FREESHIP/SO_TIEN...) không gán sản phẩm.
        // QUAN TRỌNG: chỉ coi là "chiến dịch có chủ đề" (hiện thành card riêng)
        // những khuyến mãi có targetType = CATEGORY/PRODUCT (admin đã chọn rõ
        // sản phẩm/danh mục áp dụng). Khuyến mãi targetType = ALL/trống áp dụng
        // cho MỌI sản phẩm đang giảm giá — không có chủ đề thật sự, nên đưa vào
        // đây sẽ khiến tiêu đề chiến dịch (vd "Khai trương ... giảm 15%") bị gán
        // nhầm với các sản phẩm không liên quan gì đến chủ đề đó.
        List<Promotion> campaignPromotions = activePromotions.stream()
                .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                .filter(p -> p.getTargetType() != null
                        && !p.getTargetType().isBlank()
                        && !"ALL".equalsIgnoreCase(p.getTargetType())
                        && p.getTargetIds() != null && !p.getTargetIds().isBlank())
                .limit(8)
                .toList();
        model.addAttribute("campaignPromotions", campaignPromotions);
        model.addAttribute("campaignItemsPerPage", Math.min(4, Math.max(campaignPromotions.size(), 1)));
        Map<Integer, List<Product>> campaignProductsMap = new HashMap<>();
        for (Promotion promo : campaignPromotions) {
            List<Product> prods = discountedProducts.stream()
                    .filter(p -> isPromotionApplicableToProduct(promo, p))
                    .limit(4)
                    .toList();
            campaignProductsMap.put(promo.getId(), prods);
        }
        model.addAttribute("campaignProductsMap", campaignProductsMap);

        // Filter promotions applicable to each product
        Map<Integer, Promotion> productBestPercentagePromoMap = new HashMap<>();
        Map<Integer, Promotion> productBestFixedPromoMap = new HashMap<>();

        for (Product product : allSectionProducts) {
            List<Promotion> applicablePromos = activePromotions.stream()
                    .filter(p -> isPromotionApplicableToProduct(p, product))
                    .toList();

            Promotion bestPctPromo = applicablePromos.stream()
                    .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                    .filter(p -> p.getGiaTriGiam().compareTo(new BigDecimal("100")) <= 0)
                    .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                    .max(Comparator.comparing(Promotion::getGiaTriGiam))
                    .orElse(null);

            Promotion bestFixedPromo = applicablePromos.stream()
                    .filter(p -> "SO_TIEN".equals(p.getLoaiGiam()))
                    .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                    .max(Comparator.comparing(Promotion::getGiaTriGiam))
                    .orElse(null);

            if (bestPctPromo != null) {
                productBestPercentagePromoMap.put(product.getId(), bestPctPromo);
            }
            if (bestFixedPromo != null) {
                productBestFixedPromoMap.put(product.getId(), bestFixedPromo);
            }
        }

        // Global best promos (for products without specific match)
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
        model.addAttribute("productBestPercentagePromoMap", productBestPercentagePromoMap);
        model.addAttribute("productBestFixedPromoMap", productBestFixedPromoMap);

        // Pre‑compute best price & discount per product (variant discount + promotion + flash sale)
        Map<Integer, BigDecimal> promoPriceMap = new HashMap<>();
        Map<Integer, BigDecimal> variantPromoPriceMap = new HashMap<>();
        Map<Integer, Integer> bestDiscountMap = new HashMap<>();
        // Giá gốc THỰC SỰ khớp với promoPriceMap/bestDiscountMap để hiển thị gạch ngang —
        // khi flash sale thắng thì phải lấy giaGoc của CHÍNH flash item đó (fsGiaGoc), không
        // được lấy giaGoc của biến thể "first" (khác biến thể) như trước, tránh so 1 giá với
        // 1 giá gốc của biến thể khác → % hiển thị sai/không khớp.
        Map<Integer, BigDecimal> originalPriceMap = new HashMap<>();
        // Ruy băng "FLASH SALE" (đếm ngược) chỉ hiện khi flash sale THỰC SỰ là mức giá đang
        // áp dụng — trước đây hiện bất cứ khi nào sản phẩm CÓ flash sale trên 1 biến thể nào
        // đó, dù giá hiển thị thực tế lại đang giảm theo khuyến mãi khác (đồng hồ đếm ngược
        // theo hạn flash sale không liên quan gì tới giá đang thấy) — gây cảm giác "giả/ảo".
        Map<Integer, Boolean> hasFlashMap = new HashMap<>();

        for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
            Integer productId = entry.getKey();
            List<ProductVariant> pvList = entry.getValue();
            if (pvList.isEmpty()) continue;

            ProductVariant first = pvList.get(0);
            BigDecimal giaGoc = first.getGiaGoc();
            if (giaGoc == null) giaGoc = BigDecimal.ZERO;

            // Start with best price = base price, discount = 0
            BigDecimal bestPrice = giaGoc;
            int bestPct = 0;
            BigDecimal originalPriceForDisplay = giaGoc;
            boolean hasFlash = false;

            // 1) Variant discount (giaKhuyenMai)
            if (first.getGiaKhuyenMai() != null && first.getGiaKhuyenMai().compareTo(bestPrice) < 0) {
                bestPrice = first.getGiaKhuyenMai();
                bestPct = giaGoc.subtract(bestPrice).multiply(BigDecimal.valueOf(100))
                        .divide(giaGoc, 0, java.math.RoundingMode.HALF_UP).intValue();
            }

            // 2) Promotion discount (product-specific bestPercentagePromo)
            Promotion productPctPromo = productBestPercentagePromoMap.get(productId);
            Promotion pctPromo = productPctPromo != null ? productPctPromo : bestPercentagePromo;

            if (pctPromo != null) {
                BigDecimal promoDiscountPct = pctPromo.getGiaTriGiam();
                BigDecimal promoPrice = bestPrice
                        .multiply(BigDecimal.valueOf(100).subtract(promoDiscountPct))
                        .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                if (pctPromo.getGiamToiDa() != null) {
                    BigDecimal actualDiscount = bestPrice.multiply(promoDiscountPct)
                            .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                    if (actualDiscount.compareTo(pctPromo.getGiamToiDa()) > 0) {
                        promoPrice = bestPrice.subtract(pctPromo.getGiamToiDa());
                    }
                }
                if (promoPrice.compareTo(bestPrice) < 0) {
                    bestPrice = promoPrice;
                    int pct = giaGoc.subtract(bestPrice).multiply(BigDecimal.valueOf(100))
                            .divide(giaGoc, 0, java.math.RoundingMode.HALF_UP).intValue();
                    bestPct = Math.max(bestPct, pct);
                }
            }

            // 3) Flash sale discount (overrides everything, only if it ACTUALLY beats the price so far)
            PricingService.FlashSaleOffer offer = flashSaleMap.get(productId);
            if (offer != null) {
                BigDecimal fsPrice = offer.giaSale();
                if (fsPrice.compareTo(bestPrice) < 0) {
                    BigDecimal fsGiaGoc = offer.giaGoc();
                    int pct = fsGiaGoc.compareTo(BigDecimal.ZERO) > 0
                            ? fsGiaGoc.subtract(fsPrice).multiply(BigDecimal.valueOf(100))
                                    .divide(fsGiaGoc, 0, java.math.RoundingMode.HALF_UP).intValue()
                            : 0;
                    bestPrice = fsPrice;
                    bestPct = pct;
                    originalPriceForDisplay = fsGiaGoc;
                    hasFlash = true;
                }
            }

            promoPriceMap.put(productId, bestPrice);
            bestDiscountMap.put(productId, bestPct);
            originalPriceMap.put(productId, originalPriceForDisplay);
            hasFlashMap.put(productId, hasFlash);

            // Per-variant promo map for detail use
            for (ProductVariant pv : pvList) {
                BigDecimal pvBase = pv.getGiaKhuyenMai() != null ? pv.getGiaKhuyenMai() : pv.getGiaGoc();
                if (pvBase != null) {
                    if (bestPercentagePromo != null) {
                        BigDecimal pvPromo = pvBase
                                .multiply(BigDecimal.valueOf(100).subtract(bestPercentagePromo.getGiaTriGiam()))
                                .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                        if (bestPercentagePromo.getGiamToiDa() != null) {
                            BigDecimal actualDiscount = pvBase.multiply(bestPercentagePromo.getGiaTriGiam())
                                    .divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                            if (actualDiscount.compareTo(bestPercentagePromo.getGiamToiDa()) > 0) {
                                pvPromo = pvBase.subtract(bestPercentagePromo.getGiamToiDa());
                            }
                        }
                        variantPromoPriceMap.put(pv.getId(), pvPromo.setScale(0, java.math.RoundingMode.HALF_UP));
                    } else {
                        variantPromoPriceMap.put(pv.getId(), pvBase.setScale(0, java.math.RoundingMode.HALF_UP));
                    }
                }
            }
        }

        model.addAttribute("promoPriceMap", promoPriceMap);
        model.addAttribute("variantPromoPriceMap", variantPromoPriceMap);
        model.addAttribute("bestDiscountMap", bestDiscountMap);
        model.addAttribute("originalPriceMap", originalPriceMap);
        model.addAttribute("hasFlashMap", hasFlashMap);

        // Per-variant best price (flash sale + promo + base)
        // Lưu ý: phải tra flash sale theo TỪNG biến thể (variantId), không được lấy giá
        // flash sale rẻ nhất của SẢN PHẨM (flashSaleMap theo productId) rồi áp cho mọi
        // biến thể khác — trước đây bug này khiến biến thể 750ml hiện giá flash sale
        // của biến thể 50ml dù không hề nằm trong đợt Flash Sale đó.
        Map<Integer, FlashSaleItem> variantFlashItemMap = pricingService.loadActiveFlashSaleItemMap(
                variantsMap.values().stream().flatMap(List::stream).map(ProductVariant::getId).toList());
        Map<Integer, BigDecimal> variantBestPriceMap = new HashMap<>();
        for (List<ProductVariant> pvs : variantsMap.values()) {
            for (ProductVariant pv : pvs) {
                BigDecimal pvBase = pv.getGiaKhuyenMai() != null ? pv.getGiaKhuyenMai() : pv.getGiaGoc();
                if (pvBase == null) continue;
                BigDecimal best = pvBase;
                // Flash sale — chỉ áp nếu ĐÚNG biến thể này đang có flash sale
                FlashSaleItem fsItem = variantFlashItemMap.get(pv.getId());
                if (fsItem != null) {
                    BigDecimal fsPrice = fsItem.getGiaSale();
                    if (fsPrice != null && fsPrice.compareTo(best) < 0) best = fsPrice;
                }
                // Promo
                BigDecimal promoPrice = variantPromoPriceMap.get(pv.getId());
                if (promoPrice != null && promoPrice.compareTo(best) < 0) best = promoPrice;
                variantBestPriceMap.put(pv.getId(), best);
            }
        }
        model.addAttribute("variantBestPriceMap", variantBestPriceMap);

        // Popup thông báo ảnh trang chủ
        model.addAttribute("popupPromoActive",   siteSettingService.getValue("popup_promo_active",   "0"));
        model.addAttribute("popupPromoImage",    siteSettingService.getValue("popup_promo_image",    ""));
        model.addAttribute("popupPromoLink",     siteSettingService.getValue("popup_promo_link",     ""));
        model.addAttribute("popupPromoMode",     siteSettingService.getValue("popup_promo_mode",     "once"));
        model.addAttribute("popupPromoInterval", siteSettingService.getValue("popup_promo_interval", "60"));

        return "view/client/index";
    }

    private List<Product> resolveHomepageProducts(String mode, int limit, int minPrice, int maxPrice,
            List<Integer> categoryIds) {
        switch (mode) {
            case "newest":
                return productRepository.findNewestWithVariants(PageRequest.of(0, limit)).getContent();
            case "best_sold":
            case "most_liked":
            {
                List<Object[]> rows = ("best_sold".equals(mode)
                        ? orderItemRepository.findTopSellingProductIds(PageRequest.of(0, limit))
                        : wishlistRepository.findMostLiked(PageRequest.of(0, limit)));
                List<Integer> ids = rows.stream().map(r -> (Integer) r[0]).toList();
                Map<Integer, Product> byId = productRepository.findAllByIdWithVariants(ids).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
                return ids.stream().map(byId::get).filter(Objects::nonNull).toList();
            }
            case "under_price":
                return productRepository.findUnderPrice(BigDecimal.valueOf(Math.max(minPrice, 1000)),
                        PageRequest.of(0, limit));
            case "price_range":
                if (minPrice >= maxPrice) {
                    maxPrice = minPrice + 1;
                }
                return productRepository.filterPaged(null, null, BigDecimal.valueOf(minPrice),
                        BigDecimal.valueOf(maxPrice), null, null, PageRequest.of(0, limit)).getContent();
            case "category":
                if (categoryIds.isEmpty()) {
                    return java.util.Collections.emptyList();
                }
                return productRepository.findByDanhMucIdInAndIsActiveTrue(categoryIds).stream()
                        .limit(limit).toList();
            default:
                return productService.getFeatured().stream().limit(limit).toList();
        }
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
