package com.duastore.service.admin;

import com.duastore.model.Review;
import com.duastore.model.ReviewImage;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewImageRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.FileUploadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
/**
 * phía quản trị (admin) — Service chứa nghiệp vụ (business logic) xử lý đánh giá sản phẩm.
 */
public class AdminReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    public AdminReviewService(ReviewsRepository reviewsRepository,
            ReviewImageRepository reviewImageRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            FileUploadService fileUploadService) {
        this.reviewsRepository = reviewsRepository;
        this.reviewImageRepository = reviewImageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return reviewsRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> searchAdmin(String keyword, Boolean isApproved, Integer maxScore, boolean unanswered, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewsRepository.searchAdmin(keyword, isApproved, maxScore, unanswered, pageable);
    }

    @Transactional(readOnly = true)
    public double getOverallAverageRating() {
        Double avg = reviewsRepository.getOverallAverageRating();
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public long countUnanswered() {
        return reviewsRepository.countUnanswered();
    }

    @Transactional(readOnly = true)
    public long countLowRating() {
        return reviewsRepository.countByDanhGiaLessThanEqual(2);
    }

    @Transactional(readOnly = true)
    public long countHidden() {
        return reviewsRepository.countByIsApprovedFalse();
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
        List<ReviewImage> images = reviewImageRepository.findByReviewIdOrderBySortOrderAsc(id);
        for (ReviewImage img : images) {
            fileUploadService.delete(img.getImageUrl(), "reviews");
        }
        reviewImageRepository.deleteByReviewId(id);
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
