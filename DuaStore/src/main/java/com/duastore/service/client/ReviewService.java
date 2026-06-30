package com.duastore.service.client;

import com.duastore.dto.ReviewDTO;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.model.Review;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import com.duastore.model.Product;
import com.duastore.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        List<Review> reviews = reviewsRepository
                .findByProductIdAndIsApprovedOrderByNgayTaoDesc(productId, true);
        return toDTOs(reviews);
    }

    private List<ReviewDTO> toDTOs(List<Review> reviews) {
        Set<Integer> productIds = reviews.stream().map(Review::getProductId).collect(Collectors.toSet());
        Set<Integer> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());

        Map<Integer, Product> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return reviews.stream()
                .map(r -> toDTO(r, productMap, userMap))
                .collect(Collectors.toList());
    }

    private ReviewDTO toDTO(Review review, Map<Integer, Product> productMap, Map<Integer, User> userMap) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProductId());
        dto.setUserId(review.getUserId());
        dto.setDanhGia(review.getDanhGia());
        dto.setBinhLuan(review.getBinhLuan());
        dto.setApproved(review.getIsApproved());
        dto.setNgayTao(review.getNgayTao());
        dto.setHinhAnh(review.getHinhAnh());

        Product p = productMap.get(review.getProductId());
        if (p != null) dto.setTenSanPham(p.getTenSanPham());

        User u = userMap.get(review.getUserId());
        if (u != null) dto.setHoTen(u.getHoTen());

        return dto;
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
