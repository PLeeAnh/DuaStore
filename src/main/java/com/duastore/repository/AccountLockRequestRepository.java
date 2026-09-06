package com.duastore.repository;

import com.duastore.model.AccountLockRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác yêu cầu khóa tài khoản khách hàng.
 */
public interface AccountLockRequestRepository extends JpaRepository<AccountLockRequest, Integer> {

    List<AccountLockRequest> findByStatusOrderByRequestedAtDesc(String status);

    Optional<AccountLockRequest> findFirstByUserIdAndStatusOrderByRequestedAtDesc(Integer userId, String status);

    long countByStatus(String status);

    /**
     * Quyet dinh (APPROVED/REJECTED) mot cach NGUYEN TU — chi ap dung neu request van
     * dang PENDING. Chong 2 PRODUCT_OWNER cung duyet/tu choi 1 yeu cau gan nhu cung luc:
     * neu khong co dieu kien nay, ca hai deu doc thay PENDING va deu chay side-effect
     * cua minh (vd 1 ben khoa tai khoan that su, ben kia ghi REJECTED — mau thuan du
     * lieu). Tra ve so dong bi anh huong (0 = da co nguoi xu ly truoc, 1 = thang cuoc dua).
     */
    @Modifying
    @Query("UPDATE AccountLockRequest r SET r.status = :newStatus, r.decidedBy = :decidedBy, "
            + "r.decidedAt = :decidedAt, r.decisionNote = :note "
            + "WHERE r.id = :id AND r.status = 'PENDING'")
    int decideIfPending(@Param("id") Integer id, @Param("newStatus") String newStatus,
            @Param("decidedBy") Integer decidedBy, @Param("decidedAt") LocalDateTime decidedAt,
            @Param("note") String note);
}
