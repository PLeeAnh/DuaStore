package com.duastore.service.admin;

import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import com.duastore.model.Promotion;
import com.duastore.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AdminPromotionService {

    private final PromotionRepository promotionRepository;

    public AdminPromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void autoExpirePromotions() {
        var expired = promotionRepository.findByIsActiveTrueAndDenNgayBefore(LocalDateTime.now());
        for (Promotion p : expired) {
            p.setIsActive(false);
            promotionRepository.save(p);
        }
    }

    @Transactional(readOnly = true)
    public Page<Promotion> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return promotionRepository.findAll(pageable);
    }

    public Page<Promotion> getAllPromotionsWithExpiry(int page, int size) {
        autoExpirePromotions();
        return getAllPromotions(page, size);
    }

    @Transactional(readOnly = true)
    public Page<Promotion> searchPromotions(String keyword, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (keyword != null && !keyword.isBlank()) {
            if (isActive != null) {
                return promotionRepository.findByTenChuongTrinhContainingIgnoreCaseOrMaCodeContainingIgnoreCaseAndIsActive(keyword, keyword, isActive, pageable);
            }
            return promotionRepository.findByTenChuongTrinhContainingIgnoreCaseOrMaCodeContainingIgnoreCase(keyword, keyword, pageable);
        }
        if (isActive != null) {
            return promotionRepository.findByIsActive(isActive, pageable);
        }
        return getAllPromotions(page, size);
    }

    @Transactional(readOnly = true)
    public Promotion getPromotionById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
    }

    public Promotion savePromotion(Promotion promotion) {
        String code = promotion.getMaCode().toUpperCase().trim();
        promotion.setMaCode(code);

        if (promotion.getTuNgay() != null && promotion.getDenNgay() != null
                && promotion.getTuNgay().isAfter(promotion.getDenNgay())) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        // Check trùng mã bằng DB (bỏ qua chính nó nếu là sửa)
        promotionRepository.findByMaCodeIgnoreCase(code).ifPresent(p -> {
            if (promotion.getId() == null || !promotion.getId().equals(p.getId())) {
                throw new RuntimeException("Mã giảm giá \"" + code + "\" đã tồn tại");
            }
        });

        // Nếu sửa: Giữ nguyên số lượt đã dùng
        if (promotion.getId() != null) {
            Promotion existing = getPromotionById(promotion.getId());
            promotion.setDaDung(existing.getDaDung());
        } else {
            promotion.setDaDung(0);
        }

        return promotionRepository.save(promotion);
    }

    public void deletePromotion(Integer id) {
        Promotion p = getPromotionById(id);
        p.setIsActive(false);
        promotionRepository.save(p);
    }
}
