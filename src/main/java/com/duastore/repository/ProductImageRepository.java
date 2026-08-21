package com.duastore.repository;

import com.duastore.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu sản phẩm.
 */
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    List<ProductImage> findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(Integer productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId IN :productIds AND pi.isActive = true ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImage> findByProductIdInAndIsActiveTrue(@Param("productIds") List<Integer> productIds);

    long countByIsActiveTrue();

    @Query("SELECT COUNT(DISTINCT pi.productId) FROM ProductImage pi WHERE pi.isActive = true")
    long countProductsWithImages();
}
