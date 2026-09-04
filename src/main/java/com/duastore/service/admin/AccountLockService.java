package com.duastore.service.admin;

import com.duastore.model.AccountLockRequest;
import com.duastore.model.User;
import com.duastore.repository.AccountLockRequestRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.service.NotificationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
/**
 * Khóa tài khoản khách hàng có kiểm soát: ADMIN/STAFF khóa một tài khoản không bị
 * nghi ngờ là bot phải tạo yêu cầu chờ PRODUCT_OWNER duyệt mới thực sự có hiệu lực.
 * PRODUCT_OWNER và các tài khoản bị nghi ngờ là bot (nhiều đơn dính cảnh báo gian
 * lận) được khóa ngay, không cần chờ duyệt.
 */
public class AccountLockService {

    private static final String PRODUCT_OWNER = "PRODUCT_OWNER";
    /** Từ bao nhiêu đơn dính cảnh báo gian lận trở lên thì coi tài khoản là "khả nghi bot". */
    private static final long BOT_FRAUD_ORDER_THRESHOLD = 2;

    private final AdminUserService adminUserService;
    private final AccountLockRequestRepository lockRequestRepository;
    private final OrderRepository orderRepository;
    private final AdminLogService adminLogService;
    private final NotificationHelper notificationHelper;

    public AccountLockService(AdminUserService adminUserService,
            AccountLockRequestRepository lockRequestRepository,
            OrderRepository orderRepository,
            AdminLogService adminLogService,
            NotificationHelper notificationHelper) {
        this.adminUserService = adminUserService;
        this.lockRequestRepository = lockRequestRepository;
        this.orderRepository = orderRepository;
        this.adminLogService = adminLogService;
        this.notificationHelper = notificationHelper;
    }

    @Transactional(readOnly = true)
    public boolean isSuspectedBot(Integer userId) {
        return orderRepository.countByUserIdAndFraudWarningPresent(userId) >= BOT_FRAUD_ORDER_THRESHOLD;
    }

    private boolean isProductOwner(User u) {
        return u.getRoles().stream().anyMatch(r -> PRODUCT_OWNER.equals(r.getName()));
    }

    @Transactional(readOnly = true)
    public List<AccountLockRequest> getPending() {
        return lockRequestRepository.findByStatusOrderByRequestedAtDesc("PENDING");
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return lockRequestRepository.countByStatus("PENDING");
    }

    @Transactional(readOnly = true)
    public AccountLockRequest getPendingForUser(Integer userId) {
        return lockRequestRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(userId, "PENDING")
                .orElse(null);
    }

    /**
     * @return true nếu khóa có hiệu lực ngay (PRODUCT_OWNER thao tác, hoặc tài khoản khả nghi bot);
     *         false nếu chỉ tạo yêu cầu chờ duyệt.
     */
    public boolean requestLock(Integer targetUserId, String reason, User actor) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do khóa tài khoản");
        }
        User target = adminUserService.getUserById(targetUserId);

        if (isProductOwner(actor) || isSuspectedBot(targetUserId)) {
            applyLock(target, reason.trim(), actor);
            return true;
        }

        AccountLockRequest req = new AccountLockRequest();
        req.setUserId(targetUserId);
        req.setRequestedBy(actor.getId());
        req.setReason(reason.trim());
        req = lockRequestRepository.save(req);

        adminLogService.ghiLog(actor, "Yêu cầu khóa tài khoản khách hàng #" + targetUserId,
                "CUSTOMER", targetUserId, null, null,
                "Chờ Product Owner duyệt. Lý do: " + reason.trim());
        notificationHelper.notifyStaff(
                actor.getHoTen() + " yêu cầu khóa tài khoản " + target.getHoTen() + " — đang chờ duyệt",
                "ACCOUNT_LOCK_REQUEST", req.getId(),
                "/admin/yeu-cau-khoa-tk", "Xem yêu cầu",
                "ROLE:PRODUCT_OWNER"
        );
        return false;
    }

    /**
     * approve/reject giu vung nguyen tac: chuyen trang thai PENDING -> APPROVED/REJECTED
     * BANG UPDATE NGUYEN TU TRUOC, chi chay side-effect (khoa tai khoan, ghi log, gui
     * thong bao) neu chinh request nay la nguoi "thang cuoc dua" — chong 2 PRODUCT_OWNER
     * cung duyet/tu choi 1 yeu cau gan nhu cung luc dan toi ca hai side-effect deu chay.
     */
    public void approve(Integer requestId, User productOwner, String note) {
        AccountLockRequest req = getExistingOrThrow(requestId);
        int updated = lockRequestRepository.decideIfPending(requestId, "APPROVED",
                productOwner.getId(), LocalDateTime.now(), note);
        if (updated == 0) {
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý");
        }

        User target = adminUserService.getUserById(req.getUserId());
        applyLock(target, req.getReason(), productOwner);

        adminLogService.ghiLog(productOwner, "Duyệt yêu cầu khóa tài khoản khách hàng #" + req.getUserId(),
                "CUSTOMER", req.getUserId(), null, null, "Đã đồng ý khóa");
    }

    public void reject(Integer requestId, User productOwner, String note) {
        AccountLockRequest req = getExistingOrThrow(requestId);
        int updated = lockRequestRepository.decideIfPending(requestId, "REJECTED",
                productOwner.getId(), LocalDateTime.now(), note);
        if (updated == 0) {
            throw new IllegalArgumentException("Yêu cầu này đã được xử lý");
        }

        adminLogService.ghiLog(productOwner, "Từ chối yêu cầu khóa tài khoản khách hàng #" + req.getUserId(),
                "CUSTOMER", req.getUserId(), null, null, "Đã từ chối khóa");
        notificationHelper.notifyStaff(
                productOwner.getHoTen() + " đã từ chối yêu cầu khóa tài khoản #" + req.getUserId(),
                "CUSTOMER", req.getUserId(),
                "/admin/khach-hang/" + req.getUserId(), "Xem khách hàng",
                "ROLE:PRODUCT_OWNER"
        );
    }

    private AccountLockRequest getExistingOrThrow(Integer requestId) {
        return lockRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu"));
    }

    private void applyLock(User target, String reason, User actor) {
        adminUserService.lock(target.getId(), reason, actor);

        adminLogService.ghiLog(actor, "Khóa tài khoản khách hàng #" + target.getId(),
                "CUSTOMER", target.getId(), null, null, "Lý do: " + reason);

        notificationHelper.notifyAll(
                "Tài khoản của bạn đã bị khóa. Lý do: " + reason,
                "ACCOUNT_LOCK", target.getId(), "/tai-khoan", "Xem chi tiết",
                target.getId()
        );
        notificationHelper.notifyStaff(
                actor.getHoTen() + " đã khóa tài khoản khách hàng " + target.getHoTen(),
                "CUSTOMER", target.getId(),
                "/admin/khach-hang/" + target.getId(), "Xem khách hàng",
                com.duastore.config.security.PermissionEnum.CUSTOMER_READ
        );
    }
}
