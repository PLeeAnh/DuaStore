package com.duastore.service.admin;

import com.duastore.model.Review;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AdminReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AdminReviewService(ReviewsRepository reviewsRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository) {
        this.reviewsRepository = reviewsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return reviewsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Integer id) {
        return reviewsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
    }

    public void approveReview(Integer id) {
        Review review = getReviewById(id);
        review.setIsApproved(true);
        reviewsRepository.save(review);
    }

    public void rejectReview(Integer id) {
        Review review = getReviewById(id);
        review.setIsApproved(false);
        reviewsRepository.save(review);
    }

    public void deleteReview(Integer id) {
        reviewsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public String getTenSanPham(Integer productId) {
        return productRepository.findById(productId)
                .map(p -> p.getTenSanPham())
                .orElse("SP #" + productId);
    }

    @Transactional(readOnly = true)
    public String getHoTenUser(Integer userId) {
        return userRepository.findById(userId)
                .map(u -> u.getHoTen())
                .orElse("User #" + userId);
    }
}
