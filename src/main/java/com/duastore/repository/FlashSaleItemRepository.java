package com.duastore.repository;

import com.duastore.model.FlashSaleItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
}