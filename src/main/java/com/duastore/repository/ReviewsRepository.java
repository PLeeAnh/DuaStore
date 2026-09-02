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
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu đánh giá sản phẩm.
 */
public interface ReviewsRepository extends JpaRepository<Review, Integer> {

    Page<Review> findByProductIdAndIsApproved(Integer productId, Boolean isApproved, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND (r.isApproved = true OR r.userId = :userId)")
    Page<Review> findVisibleReviews(@Param("productId") Integer productId, @Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    Page<Review> findApprovedReviews(@Param("productId") Integer productId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.isApproved = true AND r.danhGia = :rating")
    Page<Review> findApprovedReviewsByRating(@Param("productId") Integer productId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND (r.isApproved = true OR r.userId = :userId) AND r.danhGia = :rating")
    Page<Review> findVisibleReviewsByRating(@Param("productId") Integer productId, @Param("userId") Integer userId, @Param("rating") Integer rating, Pageable pageable);

    @Query("SELECT r.danhGia, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true GROUP BY r.danhGia ORDER BY r.danhGia DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Integer productId);

    List<Review> findByProductIdAndIsApprovedOrderByNgayTaoDesc(Integer productId, Boolean isApproved);

    List<Review> findByUserIdOrderByNgayTaoDesc(Integer userId);

    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    @Query("SELECT r.productId, AVG(r.danhGia), COUNT(r) FROM Review r WHERE r.isApproved = true AND r.productId IN :productIds GROUP BY r.productId")
    List<Object[]> getAverageRatings(@Param("productIds") List<Integer> productIds);

    @Query("SELECT AVG(r.danhGia), COUNT(r) FROM Review r WHERE r.productId = :productId AND r.isApproved = true")
    List<Object[]> getAverageRating(@Param("productId") Integer productId);

    @Query("SELECT r.productId, COUNT(r), AVG(r.danhGia) FROM Review r WHERE r.isApproved = true GROUP BY r.productId ORDER BY COUNT(r) DESC")
    List<Object[]> findMostReviewed(Pageable pageable);

    @Query("SELECT r FROM Review r WHERE "
            + "(:isApproved IS NULL OR r.isApproved = :isApproved) AND "
            + "(:maxScore IS NULL OR r.danhGia <= :maxScore) AND "
            + "(:unanswered = false OR r.id NOT IN (SELECT rr.reviewId FROM ReviewReply rr)) AND "
            + "(:keyword IS NULL OR :keyword = '' OR "
            + "  LOWER(r.binhLuan) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "  r.productId IN (SELECT p.id FROM Product p WHERE LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR "
            + "  r.userId IN (SELECT u.id FROM User u WHERE LOWER(u.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')))"
            + ") ORDER BY r.ngayTao DESC")
    Page<Review> searchAdmin(@Param("keyword") String keyword,
            @Param("isApproved") Boolean isApproved,
            @Param("maxScore") Integer maxScore,
            @Param("unanswered") boolean unanswered,
            Pageable pageable);

    long countByDanhGiaLessThanEqual(Integer maxScore);

    long countByIsApprovedFalse();

    @Query("SELECT COUNT(r) FROM Review r WHERE r.id NOT IN (SELECT rr.reviewId FROM ReviewReply rr)")
    long countUnanswered();

    @Query("SELECT AVG(r.danhGia) FROM Review r")
    Double getOverallAverageRating();
}
