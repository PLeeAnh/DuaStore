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
import com.duastore.service.EmailService;
import com.duastore.service.FileUploadService;
import com.duastore.util.HtmlSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final EmailService emailService;
    private final FileUploadService fileUploadService;

    public ReviewService(ReviewsRepository reviewsRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository,
            EmailService emailService,
            FileUploadService fileUploadService) {
        this.reviewsRepository = reviewsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.emailService = emailService;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ReviewDTO> getApprovedReviews(Integer productId, int page, int size, Integer currentUserId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        org.springframework.data.domain.Page<Review> reviewPage;
        if (currentUserId != null) {
            reviewPage = reviewsRepository.findVisibleReviews(productId, currentUserId, pageable);
        } else {
            reviewPage = reviewsRepository.findApprovedReviews(productId, pageable);
        }
        List<Review> reviews = reviewPage.getContent();
        Map<Integer, String> productNames = new HashMap<>();
        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> productIds = reviews.stream().map(Review::getProductId).collect(Collectors.toSet());
        Set<Integer> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());
        productRepository.findAllById(productIds).forEach(p -> productNames.put(p.getId(), p.getTenSanPham()));
        userRepository.findAllById(userIds).forEach(u -> userNames.put(u.getId(), u.getHoTen()));

        return reviewPage.map(r -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(r.getId());
            dto.setProductId(r.getProductId());
            dto.setUserId(r.getUserId());
            dto.setDanhGia(r.getDanhGia());
            dto.setBinhLuan(r.getBinhLuan());
            dto.setApproved(r.getIsApproved());
            dto.setNgayTao(r.getNgayTao());
            dto.setHinhAnh(r.getHinhAnh());
            dto.setTenSanPham(productNames.get(r.getProductId()));
            dto.setHoTen(userNames.get(r.getUserId()));
            return dto;
        });
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
    public List<ReviewDTO> getApprovedReviews(Integer productId) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "ngayTao"));
        return reviewsRepository.findByProductIdAndIsApproved(productId, true, pageable)
                .map(this::toDTO).getContent();
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
    public boolean hasCompletedOrderAndPurchased(Integer userId, Integer productId) {
        return orderItemRepository.existsByProductIdAndUserIdAndPaid(productId, userId);
    }

    @Transactional
    public ReviewDTO createReview(Integer userId, ReviewRequestDTO request, String hinhAnhUrl) {
        if (hasReviewed(userId, request.getProductId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }
        if (!hasCompletedOrderAndPurchased(userId, request.getProductId())) {
            cleanupFile(hinhAnhUrl);
            throw new RuntimeException("Bạn cần mua sản phẩm và thanh toán để được đánh giá");
        }
        if (request.getDanhGia() == null) {
            cleanupFile(hinhAnhUrl);
            throw new RuntimeException("Vui lòng chọn số sao đánh giá");
        }

        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setDanhGia(request.getDanhGia());
        review.setBinhLuan(HtmlSanitizer.sanitize(request.getBinhLuan()));
        review.setHinhAnh(hinhAnhUrl);
        review.setIsApproved(false);

        try {
            review = reviewsRepository.save(review);
        } catch (Exception e) {
            cleanupFile(hinhAnhUrl);
            throw new RuntimeException("Lỗi khi lưu đánh giá", e);
        }

        // Email notification to admin
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                emailService.send(
                        "nguyenhuydung1506@gmail.com",
                        "[DuaStore] Đánh giá mới từ " + user.getHoTen(),
                        "<p>Sản phẩm #" + request.getProductId() + "</p>"
                        + "<p>Đánh giá: " + request.getDanhGia() + " sao</p>"
                        + "<p>Nội dung: " + HtmlSanitizer.sanitize(request.getBinhLuan()) + "</p>"
                );
            }
        } catch (Exception ignored) {
        }

        return toDTO(review);
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

    @Transactional(readOnly = true)
    public List<ReviewDTO> getRecentReviewsByUser(Integer userId, int limit) {
        return reviewsRepository.findByUserIdOrderByNgayTaoDesc(userId)
                .stream().limit(limit).map(this::toDTO).toList();
    }

    private void cleanupFile(String url) {
        if (url != null) {
            try {
                fileUploadService.delete(url, "reviews");
            } catch (Exception ignored) {
            }
        }
    }
}
