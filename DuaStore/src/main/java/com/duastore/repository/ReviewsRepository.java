package com.duastore.repository;

import com.duastore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewsRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductIdAndIsApprovedOrderByNgayTaoDesc(Integer productId, Boolean isApproved);

    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);
}
