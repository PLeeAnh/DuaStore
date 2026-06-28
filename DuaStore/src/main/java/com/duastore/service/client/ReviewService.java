package com.duastore.service.client;

import com.duastore.dto.ReviewDTO;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.model.Product;
import com.duastore.model.Review;
import com.duastore.model.User;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewService(ReviewsRepository reviewsRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         OrderItemRepository orderItemRepository) {
        this.reviewsRepository = reviewsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
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

    @Transactional(readOnly = true)
    public boolean hasPurchased(Integer userId, Integer productId) {
        return orderItemRepository.existsByProductIdAndUserId(productId, userId);
    }

    @Transactional(readOnly = true)
    public boolean hasPaidAndPurchased(Integer userId, Integer productId) {
        return orderItemRepository.existsByProductIdAndUserIdAndPaid(productId, userId);
    }

    public ReviewDTO createReview(Integer userId, ReviewRequestDTO request, String hinhAnhUrl) {
        if (hasReviewed(userId, request.getProductId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }
        if (!hasPaidAndPurchased(userId, request.getProductId())) {
            throw new RuntimeException("Bạn cần mua sản phẩm và thanh toán để được đánh giá");
        }
        if (request.getDanhGia() == null) {
            throw new RuntimeException("Vui lòng chọn số sao đánh giá");
        }

        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setDanhGia(request.getDanhGia());
        review.setBinhLuan(sanitizeHtml(request.getBinhLuan()));
        review.setHinhAnh(hinhAnhUrl);
        review.setIsApproved(true);

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
        dto.setHinhAnh(review.getHinhAnh());

        productRepository.findById(review.getProductId())
                .ifPresent(p -> dto.setTenSanPham(p.getTenSanPham()));

        userRepository.findById(review.getUserId())
                .ifPresent(u -> dto.setHoTen(u.getHoTen()));

        return dto;
    }

    @Transactional(readOnly = true)
    public Map<Integer, Double> getAverageRatings(List<Integer> productIds) {
        List<Object[]> rows = reviewsRepository.getAverageRatings(productIds);
        Map<Integer, Double> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put((Integer) row[0], ((Number) row[1]).doubleValue());
        }
        return map;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRatingSummary(Integer productId) {
        List<Object[]> rows = reviewsRepository.getAverageRating(productId);
        Map<String, Object> result = new HashMap<>();
        result.put("avg", 0.0);
        result.put("count", 0L);
        if (!rows.isEmpty() && rows.get(0)[0] != null) {
            result.put("avg", ((Number) rows.get(0)[0]).doubleValue());
            result.put("count", ((Number) rows.get(0)[1]).longValue());
        }
        return result;
    }

    private String sanitizeHtml(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "");
    }
}
