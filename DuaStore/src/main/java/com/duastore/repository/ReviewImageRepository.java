package com.duastore.repository;

import com.duastore.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer> {
    List<ReviewImage> findByReviewIdOrderBySortOrderAsc(Integer reviewId);
    List<ReviewImage> findByReviewIdIn(List<Integer> reviewIds);
    void deleteByReviewId(Integer reviewId);
}
