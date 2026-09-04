package com.duastore.repository;

import com.duastore.model.FlashSaleItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu flash sale (giảm giá chớp nhoáng).
 */
public interface FlashSaleItemRepository extends JpaRepository<FlashSaleItem, Integer> {

    @Query("SELECT i FROM FlashSaleItem i JOIN i.flashSale f "
            + "WHERE i.isActive = true AND f.isActive = true "
            + "AND f.ngayBatDau <= :now AND f.ngayKetThuc >= :now")
    List<FlashSaleItem> findActiveNow(@Param("now") LocalDateTime now);

    @Query("SELECT i FROM FlashSaleItem i JOIN i.flashSale f "
            + "WHERE i.isActive = true AND f.isActive = true "
            + "AND f.ngayBatDau <= :now AND f.ngayKetThuc >= :now "
            + "AND i.variantId IN :variantIds")
    List<FlashSaleItem> findActiveByVariantIds(@Param("variantIds") Collection<Integer> variantIds,
            @Param("now") LocalDateTime now);

    @Query("SELECT i FROM FlashSaleItem i JOIN i.flashSale f "
            + "WHERE i.isActive = true AND f.isActive = true "
            + "AND f.ngayBatDau <= :now AND f.ngayKetThuc >= :now "
            + "AND i.variantId = :variantId")
    Optional<FlashSaleItem> findActiveByVariantId(@Param("variantId") Integer variantId,
            @Param("now") LocalDateTime now);

    @Query("SELECT i FROM FlashSaleItem i JOIN i.flashSale f "
            + "WHERE i.isActive = true AND f.isActive = true "
            + "AND f.ngayBatDau <= :now AND f.ngayKetThuc >= :now "
            + "AND i.variantId = :variantId ORDER BY f.priority DESC, f.id DESC")
    Optional<FlashSaleItem> findBestActiveByVariantId(@Param("variantId") Integer variantId,
            @Param("now") LocalDateTime now);

    List<FlashSaleItem> findByFlashSaleId(Integer flashSaleId);

    List<FlashSaleItem> findByVariantId(Integer variantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM FlashSaleItem i WHERE i.id = :id")
    Optional<FlashSaleItem> findByIdWithLock(@Param("id") Integer id);

    /**
     * Claim suat flash sale mot cach ATOMIC — thay the cho pattern
     * findByIdWithLock + kiem tra trong bo nho + save(), vi PESSIMISTIC_WRITE da duoc
     * xac nhan (bang test thuc te tren luong thanh toan) la KHONG chan duoc race o moi
     * truong Hibernate + SQL Server nay. UPDATE ... WHERE dieu kien la thao tac nguyen tu
     * that su o muc DB engine — 2 don hang cung tranh suat cuoi cung se khong the cung
     * "thanh cong". Tra ve so dong bi anh huong (0 = het suat/khong hop le, 1 = claim OK).
     */
    @Modifying
    @Query("UPDATE FlashSaleItem i SET i.soLuongDaBan = i.soLuongDaBan + :qty "
            + "WHERE i.id = :id AND i.isActive = true AND (i.soLuongDaBan + :qty) <= i.soLuongToiDa")
    int claimQuotaIfAvailable(@Param("id") Integer id, @Param("qty") int qty);

    /** Tra lai suat flash sale (huy don) — nguyen tu, khong cho am. */
    @Modifying
    @Query("UPDATE FlashSaleItem i SET i.soLuongDaBan = CASE WHEN i.soLuongDaBan - :qty < 0 THEN 0 ELSE i.soLuongDaBan - :qty END "
            + "WHERE i.id = :id")
    void releaseQuota(@Param("id") Integer id, @Param("qty") int qty);
}