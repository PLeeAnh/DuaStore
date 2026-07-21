package com.duastore.controller.admin;

import com.duastore.dto.PricingSuggestionDTO;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.DynamicPricingService;
import com.duastore.service.admin.PriceHistoryService;
import com.duastore.config.security.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/admin/api/dinh-gia-dong")
public class AdminPricingSuggestionController {

    private final DynamicPricingService dynamicPricingService;
    private final ProductVariantRepository variantRepository;
    private final PriceHistoryService priceHistoryService;
    private final SecurityUtil securityUtil;

    public AdminPricingSuggestionController(DynamicPricingService dynamicPricingService,
                                             ProductVariantRepository variantRepository,
                                             PriceHistoryService priceHistoryService,
                                             SecurityUtil securityUtil) {
        this.dynamicPricingService = dynamicPricingService;
        this.variantRepository = variantRepository;
        this.priceHistoryService = priceHistoryService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/variant/{variantId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public ResponseEntity<?> suggestVariant(@PathVariable Integer variantId) {
        PricingSuggestionDTO dto = dynamicPricingService.suggestForVariant(variantId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public ResponseEntity<List<PricingSuggestionDTO>> suggestProduct(@PathVariable Integer productId) {
        List<PricingSuggestionDTO> list = dynamicPricingService.suggestForProduct(productId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/ap-dung")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public ResponseEntity<Map<String, Object>> applySuggestion(@RequestBody Map<String, Object> body) {
        Integer variantId = body.get("variantId") != null ? Integer.valueOf(body.get("variantId").toString()) : null;
        String field = (String) body.get("field");
        BigDecimal value = body.get("value") != null ? new BigDecimal(body.get("value").toString()) : null;

        if (variantId == null || field == null || value == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Thiếu thông tin"));
        }

        var opt = variantRepository.findById(variantId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Không tìm thấy biến thể"));
        }

        var v = opt.get();
        BigDecimal oldPrice = "giaGoc".equals(field) ? v.getGiaGoc() : v.getGiaKhuyenMai();
        String variantName = v.getTenBienThe();
        Integer productId = v.getProductId();
        String productName = v.getProduct() != null ? v.getProduct().getTenSanPham() : "";

        if ("giaGoc".equals(field)) {
            v.setGiaGoc(value);
        } else if ("giaKhuyenMai".equals(field)) {
            v.setGiaKhuyenMai(value);
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Trường không hợp lệ"));
        }
        variantRepository.save(v);

        Integer adminId = securityUtil.getCurrentUserId();
        priceHistoryService.record(variantId, variantName, productId, productName,
                oldPrice, value, adminId, "AI_SUGGESTION");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
