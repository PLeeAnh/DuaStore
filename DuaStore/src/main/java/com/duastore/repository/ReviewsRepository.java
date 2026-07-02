package com.duastore.repository;

import com.duastore.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewsRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByProductIdAndIsApproved(Integer productId, Boolean isApproved, Pageable pageable);

    List<Review> findByProductIdAndIsApprovedOrderByNgayTaoDesc(Integer productId, Boolean isApproved);

    List<Review> findByUserIdOrderByNgayTaoDesc(Integer userId);

    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    @Query("SELECT r.productId, AVG(r.danhGia), COUNT(r) FROM Review r WHERE r.isApproved = true AND r.productId IN :productIds GROUP BY r.productId")
    List<Object[]> getAverageRatings(@Param("productIds") List<Integer> productIds);

    @Query("SELECT AVG(r.danhGia), COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    List<Object[]> getAverageRating(@Param("productId") Integer productId);
}
