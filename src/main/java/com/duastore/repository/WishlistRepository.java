package com.duastore.repository;

import com.duastore.model.Wishlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu danh sách yêu thích.
 */
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    @EntityGraph(attributePaths = {"product"})
    List<Wishlist> findByUserIdOrderByNgayThemDesc(Integer userId);

    Optional<Wishlist> findByUserIdAndProductId(Integer userId, Integer productId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    void deleteByUserIdAndProductId(Integer userId, Integer productId);

    @Query("SELECT w.productId FROM Wishlist w WHERE w.userId = ?1")
    List<Integer> findProductIdsByUserId(Integer userId);

    @Query("SELECT w.userId FROM Wishlist w WHERE w.productId = ?1")
    List<Integer> findUserIdsByProductId(Integer productId);

    @Query("SELECT w.productId, COUNT(w) FROM Wishlist w WHERE w.productId IN :productIds GROUP BY w.productId")
    List<Object[]> countByProductIds(@Param("productIds") List<Integer> productIds);

    @Query("SELECT w.productId, COUNT(w) FROM Wishlist w GROUP BY w.productId ORDER BY COUNT(w) DESC")
    List<Object[]> findMostLiked(Pageable pageable);

    long countByProductId(Integer productId);
}
