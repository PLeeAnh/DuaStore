package com.duastore.controller.client;

import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class PromotionController {

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/khuyen-mai")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<Promotion> promoPage = promotionRepository.findActiveNow(LocalDateTime.now(), PageRequest.of(page, size));
        model.addAttribute("promotions", promoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promoPage.getTotalPages());
        model.addAttribute("totalItems", promoPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("title", "khuyen-mai");
        return "view/client/promotion-list";
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
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
