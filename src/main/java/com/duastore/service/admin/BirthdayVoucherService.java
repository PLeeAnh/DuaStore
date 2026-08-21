package com.duastore.service.admin;

import com.duastore.model.Promotion;
import com.duastore.model.User;
import com.duastore.model.VoucherType;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.UserRepository;
import com.duastore.repository.UserVoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý voucher sinh nhật, voucher/mã giảm giá.
 */
public class BirthdayVoucherService {

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;

    public BirthdayVoucherService(PromotionRepository promotionRepository,
                                  UserRepository userRepository,
                                  UserVoucherRepository userVoucherRepository) {
        this.promotionRepository = promotionRepository;
        this.userRepository = userRepository;
        this.userVoucherRepository = userVoucherRepository;
    }

    @Transactional
    public int generateBirthdayVouchers() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int day = today.getDayOfMonth();

        List<Promotion> birthdayTemplates = promotionRepository.findActiveNow(LocalDateTime.now())
                .stream()
                .filter(p -> p.getVoucherType() == VoucherType.BIRTHDAY)
                .toList();

        if (birthdayTemplates.isEmpty()) {
            return 0;
        }

        Promotion template = birthdayTemplates.getFirst();

        List<User> birthdayUsers = userRepository.findAll().stream()
                .filter(u -> u.getNgaySinh() != null)
                .filter(u -> u.getNgaySinh().getMonthValue() == month
                        && u.getNgaySinh().getDayOfMonth() == day)
                .filter(u -> u.getIsActive())
                .toList();

        int count = 0;
        for (User user : birthdayUsers) {
            boolean alreadyGiven = userVoucherRepository.existsByUserIdAndPromotionId(user.getId(), template.getId());
            if (alreadyGiven) {
                continue;
            }
            try {
                com.duastore.model.UserVoucher uv = new com.duastore.model.UserVoucher();
                uv.setUserId(user.getId());
                uv.setPromotion(template);
                uv.setVoucherCode(template.getMaCode() + "-BD-" + user.getId());
                uv.setRemainingUses(1);
                uv.setExpiredAt(template.getDenNgay());
                uv.setStatus(com.duastore.model.VoucherStatus.AVAILABLE);
                uv.setTotalSaved(java.math.BigDecimal.ZERO);
                userVoucherRepository.save(uv);
                count++;
            } catch (Exception ignored) {
            }
        }
        return count;
    }
}
