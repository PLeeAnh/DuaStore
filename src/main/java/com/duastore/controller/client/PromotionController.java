package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.model.Promotion;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserVoucherRepository;
import com.duastore.service.PricingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
/**
 * Controller xử lý các request HTTP liên quan tới khuyến mãi.
 */
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final FlashSaleRepository flashSaleRepository;
    private final PricingService pricingService;
    private final UserVoucherRepository userVoucherRepository;
    private final SecurityUtil securityUtil;

    public PromotionController(PromotionRepository promotionRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            FlashSaleRepository flashSaleRepository,
            PricingService pricingService,
            UserVoucherRepository userVoucherRepository,
            SecurityUtil securityUtil) {
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.flashSaleRepository = flashSaleRepository;
        this.pricingService = pricingService;
        this.userVoucherRepository = userVoucherRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/khuyen-mai")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "0") int pPage,
            @RequestParam(defaultValue = "12") int pSize,
            Model model) {
        if (pSize != 8 && pSize != 12 && pSize != 24 && pSize != 48) {
            pSize = 12;
        }
        LocalDateTime now = LocalDateTime.now();
        Page<Promotion> promoPage = promotionRepository.findActiveNow(now, PageRequest.of(page, size));
        model.addAttribute("promotions", promoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promoPage.getTotalPages());
        model.addAttribute("totalItems", promoPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("title", "khuyen-mai");

        List<Promotion> activePromos = promotionRepository.findActiveNow(now);
        long promoCount = activePromos.size();
        String maxDiscountLabel = "";
        LocalDateTime countdownEnd = null;
        if (!activePromos.isEmpty()) {
            Optional<Promotion> bestPct = activePromos.stream()
                    .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                    .max(Comparator.comparing(Promotion::getGiaTriGiam));
            if (bestPct.isPresent()) {
                maxDiscountLabel = "Đến " + formatNum(bestPct.get().getGiaTriGiam()) + "%";
            } else {
                String moneyLabel = activePromos.stream()
                        .filter(p -> "SO_TIEN".equals(p.getLoaiGiam()))
                        .max(Comparator.comparing(Promotion::getGiaTriGiam))
                        .map(b -> "Đến " + formatNum(b.getGiaTriGiam()) + "đ")
                        .orElse("");
                maxDiscountLabel = moneyLabel;
            }
            countdownEnd = activePromos.stream()
                    .map(Promotion::getDenNgay)
                    .filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
        }
        List<FlashSale> activeFlashSales = flashSaleRepository.findActiveNow(now);
        if (!activeFlashSales.isEmpty()) {
            LocalDateTime fsEnd = activeFlashSales.stream()
                    .map(FlashSale::getNgayKetThuc)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (fsEnd != null && (countdownEnd == null || fsEnd.isAfter(countdownEnd))) {
                countdownEnd = fsEnd;
            }
        }
        model.addAttribute("promoCount", promoCount);
        model.addAttribute("maxDiscountLabel", maxDiscountLabel);
        model.addAttribute("countdownEnd", countdownEnd);

        // ── Sản phẩm đang giảm giá (phân trang riêng) ──
        int dealSize = pSize;
        Page<Product> dealPage = productRepository.findNewestWithVariants(PageRequest.of(pPage, dealSize));
        List<Integer> dealIds = dealPage.getContent().stream().map(Product::getId).toList();
        Map<Integer, List<ProductVariant>> dealVariantsMap = new HashMap<>();
        Map<Integer, PricingService.FlashSaleOffer> dealFlashSaleMap = new HashMap<>();
        if (!dealIds.isEmpty()) {
            dealVariantsMap = variantRepository.findByProductIdInAndIsActiveTrue(dealIds).stream()
                    .collect(Collectors.groupingBy(ProductVariant::getProductId));
            dealFlashSaleMap.putAll(pricingService.loadActiveFlashSaleOffers(dealIds));
        }

        BigDecimal maxPct = new BigDecimal("100");
        Promotion bestPercentagePromo = activePromos.stream()
                .filter(p -> "PHAN_TRAM".equals(p.getLoaiGiam()))
                .filter(p -> p.getGiaTriGiam().compareTo(maxPct) <= 0)
                .filter(p -> p.getSoLanDung() == null || p.getDaDung() < p.getSoLanDung())
                .max(Comparator.comparing(Promotion::getGiaTriGiam))
                .orElse(null);

        Map<Integer, BigDecimal> dealPriceMap = new HashMap<>();
        Map<Integer, BigDecimal> dealOriginalMap = new HashMap<>();
        Map<Integer, Integer> dealPctMap = new HashMap<>();
        for (Map.Entry<Integer, List<ProductVariant>> entry : dealVariantsMap.entrySet()) {
            List<ProductVariant> pvList = entry.getValue();
            if (pvList.isEmpty()) {
                continue;
            }
            // Dung dung bien the mac dinh (isDefault=true) de dai dien cho gia san pham —
            // truoc day lay pvList.get(0) (thu tu ngau nhien tu groupingBy) khien card co
            // the hien gia cua 1 bien the phu re nhat thay vi bien the mac dinh that su.
            ProductVariant first = pvList.stream()
                    .filter(ProductVariant::isDefault)
                    .findFirst()
                    .orElse(pvList.get(0));
            BigDecimal giaGoc = first.getGiaGoc();
            if (giaGoc == null) {
                giaGoc = BigDecimal.ZERO;
            }
            BigDecimal bestPrice = first.getGiaKhuyenMai() != null ? first.getGiaKhuyenMai() : giaGoc;
            int bestPct = 0;
            // Giá gốc hiển thị (gạch ngang) phải LUÔN khớp với biến thể thực sự tạo ra
            // bestPrice — nếu flash sale thắng thì phải lấy giaGoc của chính flash item
            // đó (fsGiaGoc), không được giữ nguyên giaGoc của "first" (có thể là biến thể
            // khác), tránh so giá của biến thể A với giá gốc của biến thể B.
            BigDecimal originalForDisplay = giaGoc;
            if (first.getGiaKhuyenMai() != null && first.getGiaKhuyenMai().compareTo(giaGoc) < 0) {
                bestPct = giaGoc.subtract(bestPrice).multiply(BigDecimal.valueOf(100))
                        .divide(giaGoc, 0, RoundingMode.HALF_UP).intValue();
            }
            if (bestPercentagePromo != null) {
                BigDecimal promoPrice = bestPrice
                        .multiply(BigDecimal.valueOf(100).subtract(bestPercentagePromo.getGiaTriGiam()))
                        .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                if (bestPercentagePromo.getGiamToiDa() != null) {
                    BigDecimal actualDiscount = bestPrice.multiply(bestPercentagePromo.getGiaTriGiam())
                            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
                    if (actualDiscount.compareTo(bestPercentagePromo.getGiamToiDa()) > 0) {
                        promoPrice = bestPrice.subtract(bestPercentagePromo.getGiamToiDa());
                    }
                }
                if (promoPrice.compareTo(bestPrice) < 0) {
                    bestPrice = promoPrice;
                    int pct = giaGoc.subtract(bestPrice).multiply(BigDecimal.valueOf(100))
                            .divide(giaGoc, 0, RoundingMode.HALF_UP).intValue();
                    bestPct = Math.max(bestPct, pct);
                }
            }
            PricingService.FlashSaleOffer offer = dealFlashSaleMap.get(entry.getKey());
            if (offer != null) {
                BigDecimal fsPrice = offer.giaSale();
                BigDecimal fsGiaGoc = offer.giaGoc();
                if (fsPrice.compareTo(bestPrice) < 0) {
                    int pct = fsGiaGoc.compareTo(BigDecimal.ZERO) > 0
                            ? fsGiaGoc.subtract(fsPrice).multiply(BigDecimal.valueOf(100))
                                    .divide(fsGiaGoc, 0, RoundingMode.HALF_UP).intValue()
                            : 0;
                    bestPrice = fsPrice;
                    bestPct = pct;
                    originalForDisplay = fsGiaGoc;
                }
            }
            dealPriceMap.put(entry.getKey(), bestPrice);
            dealOriginalMap.put(entry.getKey(), originalForDisplay);
            dealPctMap.put(entry.getKey(), bestPct);
        }

        model.addAttribute("dealProducts", dealPage.getContent());
        model.addAttribute("dealVariantsMap", dealVariantsMap);
        model.addAttribute("dealPriceMap", dealPriceMap);
        model.addAttribute("dealOriginalMap", dealOriginalMap);
        model.addAttribute("dealPctMap", dealPctMap);
        model.addAttribute("dealCurrentPage", pPage);
        model.addAttribute("dealTotalPages", dealPage.getTotalPages());
        model.addAttribute("dealTotalItems", dealPage.getTotalElements());
        model.addAttribute("dealPageSize", dealSize);

        return "view/client/promotion-list";
    }

    private String formatNum(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        return String.format("%,.0f", value).replace(',', '.');
    }

    @GetMapping("/khuyen-mai/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> detailJson(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Promotion p = promotionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String tuNgay = p.getTuNgay() != null ? p.getTuNgay().format(fmt) : "";
            String denNgay = p.getDenNgay() != null ? p.getDenNgay().format(fmt) : "";

            List<Map<String, Object>> related = new ArrayList<>();
            promotionRepository.findActiveNow(LocalDateTime.now(), org.springframework.data.domain.PageRequest.of(0, 5))
                    .stream()
                    .filter(r -> !r.getId().equals(id))
                    .limit(3)
                    .forEach(r -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", r.getId());
                        item.put("tenChuongTrinh", r.getTenChuongTrinh() != null ? r.getTenChuongTrinh() : "");
                        item.put("maCode", r.getMaCode() != null ? r.getMaCode() : "");
                        item.put("loaiGiam", r.getLoaiGiam() != null ? r.getLoaiGiam() : "");
                        item.put("giaTriGiam", r.getGiaTriGiam() != null ? r.getGiaTriGiam() : 0);
                        item.put("donHangToiThieu", r.getDonHangToiThieu() != null ? r.getDonHangToiThieu() : 0);
                        related.add(item);
                    });

            result.put("id", p.getId());
            result.put("tenChuongTrinh", p.getTenChuongTrinh() != null ? p.getTenChuongTrinh() : "");
            result.put("maCode", p.getMaCode() != null ? p.getMaCode() : "");
            result.put("loaiGiam", p.getLoaiGiam() != null ? p.getLoaiGiam() : "");
            result.put("giaTriGiam", p.getGiaTriGiam() != null ? p.getGiaTriGiam() : 0);
            result.put("donHangToiThieu", p.getDonHangToiThieu() != null ? p.getDonHangToiThieu() : 0);
            result.put("giamToiDa", p.getGiamToiDa() != null ? p.getGiamToiDa() : 0);
            result.put("tuNgay", tuNgay);
            result.put("denNgay", denNgay);
            result.put("stackable", Boolean.TRUE.equals(p.getStackable()) ? "Cộng dồn" : "Không cộng dồn");
            result.put("targetType", p.getTargetType() != null ? p.getTargetType() : "Tất cả");
            result.put("savedCount", p.getSavedCount() != null ? p.getSavedCount() : 0);
            result.put("related", related);
            Integer userId = securityUtil.getCurrentUserId();
            boolean ownedInWallet = userId != null
                    && userVoucherRepository.existsByUserIdAndPromotionId(userId, id);
            result.put("ownedInWallet", ownedInWallet);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
