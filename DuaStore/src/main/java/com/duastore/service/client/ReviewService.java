package com.duastore.service.client;

import com.duastore.dto.ReviewDTO;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.model.Product;
import com.duastore.model.Review;
import com.duastore.model.User;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewsRepository reviewsRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewsRepository = reviewsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getApprovedReviews(Integer productId) {
        return reviewsRepository
                .findByProductIdAndIsApprovedOrderByNgayTaoDesc(productId, true)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasReviewed(Integer userId, Integer productId) {
        return reviewsRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    public ReviewDTO createReview(Integer userId, ReviewRequestDTO request) {
        if (hasReviewed(userId, request.getProductId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setDanhGia(request.getDanhGia());
        review.setBinhLuan(request.getBinhLuan());
        review.setIsApproved(false);

        review = reviewsRepository.save(review);
        return toDTO(review);
    }

    private ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setDanhGia(review.getDanhGia());
        dto.setBinhLuan(review.getBinhLuan());
        dto.setApproved(review.getIsApproved());
        dto.setNgayTao(review.getNgayTao());

        productRepository.findById(review.getProductId())
                .ifPresent(p -> dto.setTenSanPham(p.getTenSanPham()));

        userRepository.findById(review.getUserId())
                .ifPresent(u -> dto.setHoTen(u.getHoTen()));

        return dto;
    }
}
