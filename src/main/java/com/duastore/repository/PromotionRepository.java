package com.duastore.repository;

import com.duastore.model.Promotion;
import com.duastore.model.VoucherType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
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

    /**
     * Claim 1 luot dung ma khuyen mai mot cach NGUYEN TU — thay the cho pattern
     * findByIdWithLock + kiem tra daDung/budget trong bo nho + save(), vi PESSIMISTIC_WRITE
     * da duoc xac nhan (bang test thuc te) la KHONG chan duoc race o moi truong Hibernate +
     * SQL Server nay. 2 don hang dong thoi cung dung 1 ma sap het luot/het ngan sach se
     * khong the cung "thanh cong". Tra ve so dong bi anh huong (0 = het luot/het ngan sach,
     * 1 = claim OK — daDung va usedBudget da duoc cong ngay trong cau UPDATE nay).
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.daDung = p.daDung + 1, "
            + "p.usedBudget = COALESCE(p.usedBudget, 0) + :tienGiam "
            + "WHERE p.id = :id "
            + "AND (p.soLanDung IS NULL OR p.daDung < p.soLanDung) "
            + "AND (p.budget IS NULL OR COALESCE(p.usedBudget, 0) + :tienGiam <= p.budget)")
    int claimUsageIfAvailable(@Param("id") Integer id, @Param("tienGiam") BigDecimal tienGiam);

    /**
     * Claim 1 suat "luu voucher vao vi" mot cach nguyen tu — cung ly do voi
     * claimUsageIfAvailable, chong 2 khach hang khac nhau cung luu voucher gan dung luc
     * gioi han maxClaims lam vuot han muc toan cuc.
     */
    @Modifying
    @Query("UPDATE Promotion p SET p.savedCount = COALESCE(p.savedCount, 0) + 1 "
            + "WHERE p.id = :id AND (p.maxClaims IS NULL OR COALESCE(p.savedCount, 0) < p.maxClaims)")
    int claimSaveSlotIfAvailable(@Param("id") Integer id);

    /** Tra lai suat "luu voucher" (khach xoa voucher chua dung khoi vi) — nguyen tu, khong cho am. */
    @Modifying
    @Query("UPDATE Promotion p SET p.savedCount = CASE WHEN COALESCE(p.savedCount, 0) - 1 < 0 THEN 0 ELSE COALESCE(p.savedCount, 0) - 1 END "
            + "WHERE p.id = :id")
    void releaseSaveSlot(@Param("id") Integer id);
}
