package com.duastore.controller.client;

import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PromotionController {

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @GetMapping("/khuyen-mai")
    public String list(Model model) {
        List<Promotion> promotions = promotionRepository.findActiveNow(LocalDateTime.now());
        model.addAttribute("promotions", promotions);
        model.addAttribute("title", "khuyen-mai");
        return "view/client/promotion-list";
    }

    @GetMapping("/khuyen-mai/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        model.addAttribute("promotion", promotion);
        return "view/client/promotion-detail";
    }
}
