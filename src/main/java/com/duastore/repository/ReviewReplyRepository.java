package com.duastore.repository;

import com.duastore.model.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn phản hồi đánh giá sản phẩm.
 */
@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Integer> {

    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(Integer reviewId);

    List<ReviewReply> findByReviewIdInOrderByCreatedAtAsc(List<Integer> reviewIds);

    long countByReviewId(Integer reviewId);
}
