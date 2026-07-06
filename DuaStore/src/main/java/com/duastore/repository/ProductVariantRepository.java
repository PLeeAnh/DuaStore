package com.duastore.repository;

import com.duastore.model.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    @Query("SELECT COUNT(DISTINCT v.productId) FROM ProductVariant v WHERE v.isActive = true AND v.productId IN "
            + "(SELECT v2.productId FROM ProductVariant v2 WHERE v2.isActive = true GROUP BY v2.productId HAVING SUM(v2.soLuongTon) <= :threshold)")
    long countLowStockProducts(@Param("threshold") int threshold);

    long countByIsActiveTrue();

    long countByIsActiveTrueAndSoLuongTonLessThanEqual(int threshold);

    @Query("SELECT COALESCE(SUM(v.soLuongTon), 0) FROM ProductVariant v WHERE v.isActive = true")
    long sumTotalStock();

    List<ProductVariant> findByProductIdInAndIsActiveTrue(Collection<Integer> productIds);

    List<ProductVariant> findByProductIdAndIsActiveTrue(Integer productId);

    List<ProductVariant> findByProductId(Integer productId);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Integer productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") Integer id);

    List<ProductVariant> findByIsActiveTrueOrderByIdAsc();

    Page<ProductVariant> findByIsActiveTrueOrderByIdAsc(Pageable pageable);

    @Query("SELECT v FROM ProductVariant v WHERE v.isActive = true "
            + "AND (:keyword IS NULL OR LOWER(v.tenBienThe) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY v.id ASC")
    List<ProductVariant> searchAll(@Param("keyword") String keyword);

    @Query("SELECT v FROM ProductVariant v WHERE v.isActive = true "
            + "AND (:keyword IS NULL OR LOWER(v.tenBienThe) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY v.id ASC")
    Page<ProductVariant> searchAllPaged(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId = :productId AND v.isActive = true "
            + "AND (:keyword IS NULL OR LOWER(v.tenBienThe) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "ORDER BY v.id ASC")
    List<ProductVariant> searchByProductId(@Param("productId") Integer productId,
            @Param("keyword") String keyword);

    @Query("SELECT DISTINCT v.dungTich FROM ProductVariant v WHERE v.isActive = true AND v.dungTich IS NOT NULL ORDER BY v.dungTich ASC")
    List<Integer> findDistinctDungTich();

    @Query("SELECT DISTINCT v.tenBienThe FROM ProductVariant v WHERE v.isActive = true AND v.tenBienThe IS NOT NULL ORDER BY v.tenBienThe ASC")
    List<String> findDistinctTenBienThe();
}
