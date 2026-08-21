package com.duastore.repository;

import com.duastore.model.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu giỏ hàng.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    @EntityGraph(attributePaths = {"product", "variant"})
    List<CartItem> findByUserIdOrderByNgayThemDesc(Integer userId);

    @EntityGraph(attributePaths = {"product", "variant"})
    Optional<CartItem> findByUserIdAndVariantId(Integer userId, Integer variantId);

    int countByUserId(Integer userId);

    void deleteByIdAndUserId(Integer id, Integer userId);

    @Query("SELECT c FROM CartItem c WHERE c.ngayThem < :cutoff")
    List<CartItem> findOldItems(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT c.productId FROM CartItem c WHERE c.userId = :userId")
    List<Integer> findProductIdsByUserId(@Param("userId") Integer userId);
}
