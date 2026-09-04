package com.duastore.service;

import com.duastore.model.LoyaltyBalance;
import com.duastore.model.LoyaltyTransaction;
import com.duastore.repository.LoyaltyBalanceRepository;
import com.duastore.repository.LoyaltyTransactionRepository;
import com.duastore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý điểm thưởng/tích lũy.
 */
public class LoyaltyPointsService {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyPointsService.class);

    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final LoyaltyBalanceRepository loyaltyBalanceRepository;
    private final UserRepository userRepository;
    private final SiteSettingService siteSettingService;

    public LoyaltyPointsService(LoyaltyTransactionRepository loyaltyTransactionRepository,
            LoyaltyBalanceRepository loyaltyBalanceRepository,
            UserRepository userRepository,
            SiteSettingService siteSettingService) {
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
        this.loyaltyBalanceRepository = loyaltyBalanceRepository;
        this.userRepository = userRepository;
        this.siteSettingService = siteSettingService;
    }

    /**
     * Dam bao co dong so du cho user (tao voi balance=0 neu chua co) truoc khi thao tac
     * atomic — UPDATE...WHERE khong tu tao dong moi. Bat PK-violation neu 2 request cung
     * tao lan dau gan nhu cung luc (chi anh huong lan giao dich diem DAU TIEN cua user).
     */
    private void ensureBalanceRow(Integer userId) {
        if (!loyaltyBalanceRepository.existsById(userId)) {
            try {
                LoyaltyBalance b = new LoyaltyBalance();
                b.setUserId(userId);
                b.setBalance(0);
                loyaltyBalanceRepository.save(b);
            } catch (Exception ignored) {
                // Da duoc tao boi 1 request khac gan nhu cung luc — bo qua.
            }
        }
    }

    private int currentBalance(Integer userId) {
        return loyaltyBalanceRepository.findById(userId).map(LoyaltyBalance::getBalance).orElse(0);
    }

    public int getPointsEarnRate() {
        String rate = siteSettingService.getValue("loyalty_earn_rate", "10000");
        try {
            return Integer.parseInt(rate);
        } catch (NumberFormatException e) {
            return 10000;
        }
    }

    public int getPointsExpiryMonths() {
        String months = siteSettingService.getValue("loyalty_expiry_months", "12");
        try {
            return Integer.parseInt(months);
        } catch (NumberFormatException e) {
            return 12;
        }
    }

    public void setPointsExpiryMonths(int months) {
        siteSettingService.save("loyalty_expiry_months", String.valueOf(months), "loyalty");
    }

    public int getPointsRedeemRate() {
        String rate = siteSettingService.getValue("loyalty_redeem_rate", "100");
        try {
            return Integer.parseInt(rate);
        } catch (NumberFormatException e) {
            return 100;
        }
    }

    public void setPointsEarnRate(int rate) {
        siteSettingService.save("loyalty_earn_rate", String.valueOf(rate), "loyalty");
    }

    public void setPointsRedeemRate(int rate) {
        siteSettingService.save("loyalty_redeem_rate", String.valueOf(rate), "loyalty");
    }

    public BigDecimal convertPointsToMoney(int points) {
        return BigDecimal.valueOf(points).multiply(BigDecimal.valueOf(getPointsRedeemRate()));
    }

    @Transactional
    public void earnPoints(Integer userId, Integer orderId, BigDecimal orderAmount) {
        try {
            // Chan cong diem trung: don co the duoc goi hoan thanh tu 2 nguon (khach tu bam
            // "Da nhan hang" VA admin doi trang thai) gan nhu cung luc, hoac 1 nguon bi goi
            // lai (double-click/retry). Khong co check nay, moi lan goi la cong them 1 lan.
            if (loyaltyTransactionRepository.findFirstByUserIdAndReferenceIdAndType(userId, orderId, "EARNED").isPresent()) {
                log.info("Bo qua tich diem trung cho don {} (user {}) — da tich diem tu truoc", orderId, userId);
                return;
            }
            int rate = getPointsEarnRate();
            int points = orderAmount.divideToIntegralValue(BigDecimal.valueOf(rate)).intValue();
            if (points <= 0) return;
            ensureBalanceRow(userId);
            loyaltyBalanceRepository.addDelta(userId, points);
            int newBalance = currentBalance(userId);
            LoyaltyTransaction tx = new LoyaltyTransaction();
            tx.setUserId(userId);
            tx.setPoints(points);
            tx.setBalance(newBalance);
            tx.setType("EARNED");
            tx.setReferenceId(orderId);
            tx.setNote("Tích điểm từ đơn hàng #" + orderId);
            loyaltyTransactionRepository.save(tx);
        } catch (Exception e) {
            log.warn("Loi tich diem cho user {}: {}", userId, e.getMessage());
        }
    }

    @Transactional
    public int redeemPoints(Integer userId, int points, String note) {
        return redeemPoints(userId, points, null, note);
    }

    /**
     * Tru diem NGUYEN TU qua LoyaltyBalanceRepository.deductIfEnough truoc khi ghi log —
     * chong khach double-submit checkout (2 tab, bam nhieu lan) doi diem 2 lan gan nhu
     * cung luc, cung doc thay du diem truoc khi ben nao commit (y het lop bug da sua o
     * thanh toan/tong kho/khuyen mai — pessimistic lock da bi xac nhan khong dang tin
     * cay o moi truong Hibernate + SQL Server nay).
     */
    @Transactional
    public int redeemPoints(Integer userId, int points, Integer orderId, String note) {
        ensureBalanceRow(userId);
        int updated = loyaltyBalanceRepository.deductIfEnough(userId, points);
        if (updated == 0) {
            throw new IllegalArgumentException("Không đủ điểm. Hiện có: " + currentBalance(userId) + ", cần: " + points);
        }
        int newBalance = currentBalance(userId);
        LoyaltyTransaction tx = new LoyaltyTransaction();
        tx.setUserId(userId);
        tx.setPoints(-points);
        tx.setBalance(newBalance);
        tx.setType("REDEEMED");
        tx.setReferenceId(orderId);
        tx.setNote(note);
        return loyaltyTransactionRepository.save(tx).getBalance();
    }

    /**
     * Hoan lai diem da doi cho 1 don hang (dung khi huy don). Vo hai neu don
     * khong redeem diem nao (khong tim thay giao dich REDEEMED tuong ung).
     */
    @Transactional
    public void refundRedeemedPointsForOrder(Integer userId, Integer orderId) {
        loyaltyTransactionRepository.findFirstByUserIdAndReferenceIdAndType(userId, orderId, "REDEEMED")
                .ifPresent(redeemed -> {
                    int points = -redeemed.getPoints();
                    if (points <= 0) return;
                    ensureBalanceRow(userId);
                    loyaltyBalanceRepository.addDelta(userId, points);
                    int newBalance = currentBalance(userId);
                    LoyaltyTransaction refund = new LoyaltyTransaction();
                    refund.setUserId(userId);
                    refund.setPoints(points);
                    refund.setBalance(newBalance);
                    refund.setType("ADJUSTED");
                    refund.setReferenceId(orderId);
                    refund.setNote("Hoàn điểm do hủy đơn #" + orderId);
                    loyaltyTransactionRepository.save(refund);
                });
    }

    @Transactional
    public int adjustPoints(Integer userId, int points, String reason, String adminName) {
        ensureBalanceRow(userId);
        int before = currentBalance(userId);
        int delta = points < 0 && -points > before ? -before : points;
        if (delta != 0) {
            loyaltyBalanceRepository.addDelta(userId, delta);
        }
        int newBalance = currentBalance(userId);
        LoyaltyTransaction tx = new LoyaltyTransaction();
        tx.setUserId(userId);
        tx.setPoints(delta);
        tx.setBalance(newBalance);
        tx.setType("ADJUSTED");
        tx.setNote(reason + " (bởi " + adminName + ")");
        loyaltyTransactionRepository.save(tx);
        return newBalance;
    }

    public int getBalance(Integer userId) {
        ensureBalanceRow(userId);
        return currentBalance(userId);
    }

    @Transactional
    public void expireOldPoints() {
        int months = getPointsExpiryMonths();
        LocalDateTime threshold = LocalDateTime.now().minusMonths(months);

        List<Integer> userIds = loyaltyTransactionRepository.findUserIdsWithOldEarnedTransactions(threshold);

        for (Integer userId : userIds) {
            try {
                expireOldPointsForUser(userId, threshold);
            } catch (Exception e) {
                log.warn("Loi het han diem cho user {}: {}", userId, e.getMessage());
            }
        }
    }

    @Transactional
    public void expireOldPointsForUser(Integer userId, LocalDateTime threshold) {
        List<LoyaltyTransaction> oldTransactions = loyaltyTransactionRepository
                .findOldEarnedTransactionsForExpiry(userId, threshold);

        if (oldTransactions.isEmpty()) {
            return;
        }

        int currentBalance = currentBalance(userId);
        int pointsToExpire = 0;

        for (LoyaltyTransaction tx : oldTransactions) {
            // Only expire points that haven't been used/redeemed yet
            // We check if there's enough balance to cover this transaction
            if (currentBalance >= tx.getPoints()) {
                pointsToExpire += tx.getPoints();
                currentBalance -= tx.getPoints();

                // Mark the transaction as expired
                tx.setType("EXPIRED");
                tx.setNote("Hết hạn sau " + getPointsExpiryMonths() + " tháng không hoạt động");
                loyaltyTransactionRepository.save(tx);
            } else {
                // Not enough balance to expire this transaction fully
                break;
            }
        }

        if (pointsToExpire > 0) {
            ensureBalanceRow(userId);
            loyaltyBalanceRepository.addDelta(userId, -pointsToExpire);
            // Record the expiry as an adjustment
            LoyaltyTransaction expiryTx = new LoyaltyTransaction();
            expiryTx.setUserId(userId);
            expiryTx.setPoints(-pointsToExpire);
            expiryTx.setBalance(currentBalance(userId));
            expiryTx.setType("EXPIRED");
            expiryTx.setNote("Hết hạn " + pointsToExpire + " điểm sau " + getPointsExpiryMonths() + " tháng không hoạt động");
            loyaltyTransactionRepository.save(expiryTx);
        }
    }

    public void setPointsExpiryEnabled(boolean enabled) {
        siteSettingService.save("loyalty_expiry_enabled", String.valueOf(enabled), "loyalty");
    }

    public boolean isPointsExpiryEnabled() {
        String val = siteSettingService.getValue("loyalty_expiry_enabled", "true");
        return Boolean.parseBoolean(val);
    }
}
