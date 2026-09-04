package com.duastore.repository;

import com.duastore.model.UserVoucher;
import com.duastore.model.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu người dùng, voucher/mã giảm giá.
 */
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {

    @EntityGraph(attributePaths = {"promotion"})
    List<UserVoucher> findByUserIdOrderBySavedAtDesc(Integer userId);

    @EntityGraph(attributePaths = {"promotion"})
    Page<UserVoucher> findByUserIdAndStatus(Integer userId, VoucherStatus status, Pageable pageable);

    Optional<UserVoucher> findByUserIdAndPromotionId(Integer userId, Integer promotionId);

    boolean existsByUserIdAndPromotionId(Integer userId, Integer promotionId);

    void deleteByPromotionId(Integer promotionId);

    long countByUserIdAndStatus(Integer userId, VoucherStatus status);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId "
            + "AND uv.expiredAt BETWEEN :now AND :soon AND uv.status = 'AVAILABLE'")
    List<UserVoucher> findExpiringSoon(@Param("userId") Integer userId,
            @Param("now") LocalDateTime now,
            @Param("soon") LocalDateTime soon);

    @Modifying
    @Query("UPDATE UserVoucher uv SET uv.status = 'EXPIRED' "
            + "WHERE uv.expiredAt < :now AND uv.status = 'AVAILABLE'")
    int expireVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId AND uv.status = 'AVAILABLE' "
            + "AND (uv.expiredAt IS NULL OR uv.expiredAt >= :now)")
    List<UserVoucher> findAvailableByUserId(@Param("userId") Integer userId,
            @Param("now") LocalDateTime now);

    @Query("SELECT uv FROM UserVoucher uv JOIN FETCH uv.promotion p WHERE uv.userId = :userId AND uv.status = :status "
            + "AND (:keyword IS NULL OR LOWER(p.tenChuongTrinh) LIKE LOWER(CONCAT('%', COALESCE(:keyword, ''), '%')) "
            + "OR LOWER(p.maCode) LIKE LOWER(CONCAT('%', COALESCE(:keyword, ''), '%')))")
    Page<UserVoucher> searchByUserIdAndStatus(@Param("userId") Integer userId,
            @Param("status") VoucherStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT uv.status, COUNT(uv) FROM UserVoucher uv GROUP BY uv.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT uv.promotion.id, COUNT(uv) FROM UserVoucher uv WHERE uv.status = 'USED' GROUP BY uv.promotion.id ORDER BY COUNT(uv) DESC")
    List<Object[]> countUsedGroupByPromotionId(Pageable pageable);

    /**
     * Tru 1 luot dung voucher mot cach nguyen tu — chong khach double-submit checkout
     * (bam "Đặt hàng" 2 lan lien tiep) khien cung 1 voucher bi tru luot 2 lan. Neu day la
     * luot cuoi thi tu chuyen sang USED trong cung 1 UPDATE. Tra ve 0 = voucher da het
     * luot/het han/khong con AVAILABLE (caller phai bao loi), 1 = tru luot thanh cong.
     */
    @Modifying
    @Query("UPDATE UserVoucher uv SET uv.remainingUses = uv.remainingUses - 1, "
            + "uv.status = CASE WHEN uv.remainingUses - 1 <= 0 THEN 'USED' ELSE uv.status END, "
            + "uv.usedAt = CASE WHEN uv.remainingUses - 1 <= 0 THEN :now ELSE uv.usedAt END, "
            + "uv.totalSaved = uv.totalSaved + :tienGiam "
            + "WHERE uv.id = :id AND uv.status = 'AVAILABLE' AND uv.remainingUses > 0 "
            + "AND (uv.expiredAt IS NULL OR uv.expiredAt >= :now)")
    int consumeUseIfAvailable(@Param("id") Integer id, @Param("tienGiam") java.math.BigDecimal tienGiam,
            @Param("now") LocalDateTime now);
}
