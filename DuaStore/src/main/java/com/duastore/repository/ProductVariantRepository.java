package com.duastore.repository;

import com.duastore.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductIdInAndIsActiveTrue(Collection<Integer> productIds);
    List<ProductVariant> findByProductIdAndIsActiveTrue(Integer productId);
    List<ProductVariant> findByProductId(Integer productId);
    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Integer productId);

    @Query("SELECT v FROM ProductVariant v WHERE v.productId = :productId AND v.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(v.tenBienThe) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY v.id ASC")
    List<ProductVariant> searchByProductId(@Param("productId") Integer productId,
                                           @Param("keyword") String keyword);
}
