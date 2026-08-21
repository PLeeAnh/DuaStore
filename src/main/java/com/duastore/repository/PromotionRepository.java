package com.duastore.repository;

import com.duastore.model.Promotion;
import com.duastore.model.VoucherType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu khuyến mãi.
 */
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByMaCodeAndIsActiveTrue(String maCode);

    Optional<Promotion> findByMaCodeIgnoreCase(String maCode);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND UPPER(p.maCode) = UPPER(:maCode)")
    Optional<Promotion> findByMaCodeIgnoreCaseAndIsActiveTrue(String maCode);

    List<Promotion> findByIsActiveTrueAndDenNgayBefore(LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.tuNgay IS NULL OR p.tuNgay <= :now) AND (p.denNgay IS NULL OR p.denNgay >= :now) ORDER BY p.priority DESC, p.id DESC")
    List<Promotion> findActiveNow(LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.tuNgay IS NULL OR p.tuNgay <= :now) AND (p.denNgay IS NULL OR p.denNgay >= :now) ORDER BY p.priority DESC, p.id DESC")
    Page<Promotion> findActiveNow(LocalDateTime now, Pageable pageable);

    List<Promotion> findByIsActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Promotion p WHERE p.id = :id")
    Optional<Promotion> findByIdWithLock(Integer id);

    Page<Promotion> findByTenChuongTrinhContainingIgnoreCaseOrMaCodeContainingIgnoreCase(String ten, String ma, Pageable pageable);

    Page<Promotion> findByIsActive(Boolean isActive, Pageable pageable);

    Page<Promotion> findByTenChuongTrinhContainingIgnoreCaseOrMaCodeContainingIgnoreCaseAndIsActive(String ten, String ma, Boolean isActive, Pageable pageable);

    Page<Promotion> findByVoucherType(VoucherType voucherType, Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.denNgay >= :now ORDER BY p.priority DESC, p.savedCount DESC")
    List<Promotion> findFeaturedPromotions(@Param("now") LocalDateTime now, Pageable pageable);

    long countByIsActiveTrue();

    long countByIsActiveFalse();
}
