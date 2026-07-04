package com.duastore.service.client;

import com.duastore.model.Promotion;
import com.duastore.model.UserVoucher;
import com.duastore.model.VoucherStatus;
import com.duastore.model.VoucherType;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserVoucherRepository;
import org.springframework.dao.DataIntegrityViolationException;
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
        Promotion lockedPromo = promotionRepository.findByIdWithLock(promotionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
        if (!Boolean.TRUE.equals(lockedPromo.getIsActive())) {
            throw new RuntimeException("Khuyến mãi không còn hiệu lực");
        }
        if (lockedPromo.getMaxClaimsPerUser() != null && lockedPromo.getMaxClaimsPerUser() <= 0) {
            throw new RuntimeException("Khuyến mãi này không cho phép lưu voucher");
        }
        if (lockedPromo.getMaxClaims() != null
                && lockedPromo.getSavedCount() != null
                && lockedPromo.getSavedCount() >= lockedPromo.getMaxClaims()) {
            throw new RuntimeException("Khuyến mãi đã hết lượt lưu");
        }

        UserVoucher uv = new UserVoucher();
        uv.setUserId(userId);
        uv.setPromotion(lockedPromo);
        uv.setVoucherCode(lockedPromo.getMaCode() + "-" + userId);
        uv.setRemainingUses(lockedPromo.getMaxClaimsPerUser());
        uv.setExpiredAt(lockedPromo.getDenNgay());
        uv.setStatus(VoucherStatus.AVAILABLE);
        uv.setTotalSaved(java.math.BigDecimal.ZERO);

        lockedPromo.setSavedCount(lockedPromo.getSavedCount() != null ? lockedPromo.getSavedCount() + 1 : 1);
        promotionRepository.save(lockedPromo);

        try {
            return userVoucherRepository.save(uv);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Voucher đã có trong ví");
        }
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
    public Page<UserVoucher> getWalletByTabWithFilters(Integer userId, VoucherStatus status,
                                                        String keyword, String sortBy,
                                                        int page, int size) {
        Sort sort = buildSort(sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        if (keyword != null && !keyword.isBlank()) {
            return userVoucherRepository.searchByUserIdAndStatus(userId, status, keyword.trim(), pageable);
        }
        return userVoucherRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    private Sort buildSort(String sortBy) {
        if (sortBy == null) sortBy = "default";
        return switch (sortBy) {
            case "expiry" -> Sort.by(Sort.Direction.ASC, "expiredAt");
            case "saved" -> Sort.by(Sort.Direction.DESC, "savedAt");
            default -> Sort.by(Sort.Direction.DESC, "savedAt");
        };
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
