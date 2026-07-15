package com.duastore.controller.client;

import com.duastore.model.Product;
import com.duastore.service.RecommendationService;
import com.duastore.config.security.SecurityUtil;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final SecurityUtil securityUtil;

    public RecommendationController(RecommendationService recommendationService,
                                    SecurityUtil securityUtil) {
        this.recommendationService = recommendationService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/personalized")
    public List<Map<String, Object>> getPersonalizedSuggestions(
            @RequestParam(defaultValue = "8") int limit) {
        Integer userId = securityUtil.getCurrentUserId();
        List<Product> products = recommendationService.getPersonalizedSuggestions(userId, limit);

        return products.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("tenSanPham", p.getTenSanPham());
            m.put("hinhAnhChinh", p.getHinhAnhChinh());
            m.put("minPrice", p.getMinPrice());
            m.put("thuongHieu", p.getThuongHieu());
            m.put("danhMucId", p.getDanhMucId());
            return m;
        }).collect(Collectors.toList());
    }
}
