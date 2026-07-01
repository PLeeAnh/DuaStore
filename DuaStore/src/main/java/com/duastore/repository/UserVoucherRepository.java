package com.duastore.repository;

import com.duastore.model.UserVoucher;
import com.duastore.model.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Integer> {

    List<UserVoucher> findByUserIdOrderBySavedAtDesc(Integer userId);

    Page<UserVoucher> findByUserIdAndStatus(Integer userId, VoucherStatus status, Pageable pageable);

    Optional<UserVoucher> findByUserIdAndPromotionId(Integer userId, Integer promotionId);

    boolean existsByUserIdAndPromotionId(Integer userId, Integer promotionId);

    long countByUserIdAndStatus(Integer userId, VoucherStatus status);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId " +
           "AND uv.expiredAt BETWEEN :now AND :soon AND uv.status = 'AVAILABLE'")
    List<UserVoucher> findExpiringSoon(@Param("userId") Integer userId,
                                       @Param("now") LocalDateTime now,
                                       @Param("soon") LocalDateTime soon);

    @Modifying
    @Query("UPDATE UserVoucher uv SET uv.status = 'EXPIRED' " +
           "WHERE uv.expiredAt < :now AND uv.status = 'AVAILABLE'")
    int expireVouchers(@Param("now") LocalDateTime now);

    @Query("SELECT uv FROM UserVoucher uv WHERE uv.userId = :userId AND uv.status = 'AVAILABLE' " +
           "AND (uv.expiredAt IS NULL OR uv.expiredAt >= :now)")
    List<UserVoucher> findAvailableByUserId(@Param("userId") Integer userId,
                                            @Param("now") LocalDateTime now);
}
