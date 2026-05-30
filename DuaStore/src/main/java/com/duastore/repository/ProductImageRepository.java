package com.duastore.repository;

import com.duastore.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(Integer productId);
}
