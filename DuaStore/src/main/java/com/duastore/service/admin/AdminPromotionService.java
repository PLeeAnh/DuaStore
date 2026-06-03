package com.duastore.service.admin;

import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminPromotionService {

    private final PromotionRepository promotionRepository;

    public AdminPromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Promotion> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return promotionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
    }

    public Promotion savePromotion(Promotion promotion) {
        if (promotion.getId() != null) {
            Promotion existing = getPromotionById(promotion.getId());
            promotion.setDaDung(existing.getDaDung());
        } else {
            if (promotion.getDaDung() == null) promotion.setDaDung(0);
            if (promotion.getIsActive() == null) promotion.setIsActive(true);
        }
        if (promotion.getMaCode() != null) {
            String code = promotion.getMaCode().toUpperCase().trim();
            promotionRepository.findAll().stream()
                    .filter(p -> p.getMaCode().equalsIgnoreCase(code))
                    .filter(p -> promotion.getId() == null || !promotion.getId().equals(p.getId()))
                    .findFirst()
                    .ifPresent(p -> {
                        throw new RuntimeException("Mã giảm giá \"" + promotion.getMaCode() + "\" đã tồn tại");
                    });
        }
        return promotionRepository.save(promotion);
    }

    public void deletePromotion(Integer id) {
        Promotion p = getPromotionById(id);
        p.setIsActive(false);
        promotionRepository.save(p);
    }
}
