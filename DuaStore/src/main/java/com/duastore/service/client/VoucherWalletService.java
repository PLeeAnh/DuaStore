package com.duastore.service.client;

import com.duastore.model.Promotion;
import com.duastore.model.UserVoucher;
import com.duastore.model.VoucherStatus;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserVoucherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VoucherWalletService {

    private final UserVoucherRepository userVoucherRepository;
    private final PromotionRepository promotionRepository;

    public VoucherWalletService(UserVoucherRepository userVoucherRepository,
                                PromotionRepository promotionRepository) {
        this.userVoucherRepository = userVoucherRepository;
        this.promotionRepository = promotionRepository;
    }

    public UserVoucher saveVoucher(Integer userId, Integer promotionId) {
        if (userVoucherRepository.existsByUserIdAndPromotionId(userId, promotionId)) {
            throw new RuntimeException("Voucher đã có trong ví");
        }
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        if (!Boolean.TRUE.equals(promotion.getIsActive())) {
            throw new RuntimeException("Khuyến mãi không còn hiệu lực");
        }

        UserVoucher uv = new UserVoucher();
        uv.setUserId(userId);
        uv.setPromotion(promotion);
        uv.setVoucherCode(promotion.getMaCode() + "-" + userId);
        uv.setRemainingUses(promotion.getMaxClaimsPerUser());
        uv.setExpiredAt(promotion.getDenNgay());
        uv.setStatus(VoucherStatus.AVAILABLE);
        uv.setTotalSaved(java.math.BigDecimal.ZERO);

        promotion.setSavedCount(promotion.getSavedCount() != null ? promotion.getSavedCount() + 1 : 1);
        promotionRepository.save(promotion);

        return userVoucherRepository.save(uv);
    }

    public void removeVoucher(Integer userId, Integer voucherId) {
        UserVoucher uv = userVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        if (!uv.getUserId().equals(userId)) {
            throw new RuntimeException("Không có quyền xóa voucher này");
        }
        userVoucherRepository.delete(uv);
    }

    @Transactional(readOnly = true)
    public List<UserVoucher> getWallet(Integer userId) {
        return userVoucherRepository.findByUserIdOrderBySavedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Page<UserVoucher> getWalletByTab(Integer userId, VoucherStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "savedAt"));
        return userVoucherRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    @Transactional(readOnly = true)
    public long countAvailable(Integer userId) {
        return userVoucherRepository.countByUserIdAndStatus(userId, VoucherStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<UserVoucher> getAvailableVouchers(Integer userId) {
        return userVoucherRepository.findAvailableByUserId(userId, LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void autoExpireVouchers() {
        userVoucherRepository.expireVouchers(LocalDateTime.now());
    }
}
