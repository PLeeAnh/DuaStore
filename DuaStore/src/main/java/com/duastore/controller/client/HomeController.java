package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Category;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserVoucherRepository;
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
    private final PricingService pricingService;

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
                          PricingService pricingService) {
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
        this.pricingService = pricingService;
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
            case 1 -> "col-md-12";
            case 2 -> "col-md-6";
            case 3 -> "col-md-4";
            case 4 -> "col-md-3";
            default -> "col-md";
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
        try { currentUserId = securityUtil.getCurrentUserId(); } catch (Exception ignored) {}
        if (currentUserId != null) {
            model.addAttribute("voucherCount", userVoucherRepository.countByUserIdAndStatus(currentUserId, VoucherStatus.AVAILABLE));
        }

        // Active banners for hero section
        model.addAttribute("banners", bannerService.getActiveForClient());

        List<Product> featured = productService.getFeatured();
        model.addAttribute("featuredProducts", featured.stream().limit(prodLimit).toList());
        model.addAttribute("featuredCategories", categoryService.getFeaturedCategories().stream().limit(catLimit).toList());

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

        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        if (!featured.isEmpty()) {
            List<Integer> ids = featured.stream().map(Product::getId).collect(Collectors.toList());
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
                    if (parts.length >= 2) kieuNap = parts[1].trim();
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
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return defaultValue; }
    }

}