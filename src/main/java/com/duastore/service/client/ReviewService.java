package com.duastore.service.client;

import com.duastore.dto.ReviewDTO;
import com.duastore.dto.ReviewRequestDTO;
import com.duastore.model.Review;
import com.duastore.model.ReviewImage;
import com.duastore.repository.OrderItemRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewImageRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserRepository;
import com.duastore.model.User;
import com.duastore.service.AsyncEmailService;
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
/**
 * Service chứa nghiệp vụ (business logic) xử lý đánh giá sản phẩm.
 */
public class ReviewService {

    private final ReviewsRepository reviewsRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final AsyncEmailService asyncEmailService;
    private final FileUploadService fileUploadService;

    public ReviewService(ReviewsRepository reviewsRepository,
            ReviewImageRepository reviewImageRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            OrderItemRepository orderItemRepository,
            AsyncEmailService asyncEmailService,
            FileUploadService fileUploadService) {
        this.reviewsRepository = reviewsRepository;
        this.reviewImageRepository = reviewImageRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.asyncEmailService = asyncEmailService;
        this.fileUploadService = fileUploadService;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ReviewDTO> getApprovedReviews(Integer productId, int page, int size, Integer currentUserId, Integer ratingFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayTao"));
        org.springframework.data.domain.Page<Review> reviewPage;
        if (ratingFilter != null && ratingFilter >= 1 && ratingFilter <= 5) {
            if (currentUserId != null) {
                reviewPage = reviewsRepository.findVisibleReviewsByRating(productId, currentUserId, ratingFilter, pageable);
            } else {
                reviewPage = reviewsRepository.findApprovedReviewsByRating(productId, ratingFilter, pageable);
            }
        } else {
            if (currentUserId != null) {
                reviewPage = reviewsRepository.findVisibleReviews(productId, currentUserId, pageable);
            } else {
                reviewPage = reviewsRepository.findApprovedReviews(productId, pageable);
            }
        }
        List<Review> reviews = reviewPage.getContent();
        Map<Integer, String> productNames = new HashMap<>();
        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> productIds = reviews.stream().map(Review::getProductId).collect(Collectors.toSet());
        Set<Integer> userIds = reviews.stream().map(Review::getUserId).collect(Collectors.toSet());
        productRepository.findAllById(productIds).forEach(p -> productNames.put(p.getId(), p.getTenSanPham()));
        userRepository.findAllById(userIds).forEach(u -> userNames.put(u.getId(), u.getHoTen()));

        Map<Integer, List<String>> reviewImages = new HashMap<>();
        if (!reviews.isEmpty()) {
            List<ReviewImage> allImages = reviewImageRepository.findByReviewIdIn(reviews.stream().map(Review::getId).toList());
            for (ReviewImage img : allImages) {
                reviewImages.computeIfAbsent(img.getReviewId(), k -> new java.util.ArrayList<>()).add(img.getImageUrl());
            }
        }

        return reviewPage.map(r -> {
            ReviewDTO dto = new ReviewDTO();
            dto.setId(r.getId());
            dto.setProductId(r.getProductId());
            dto.setUserId(r.getUserId());
            dto.setDanhGia(r.getDanhGia());
            dto.setBinhLuan(r.getBinhLuan());
            dto.setApproved(r.getIsApproved());
            dto.setNgayTao(r.getNgayTao());
            dto.setHinhAnhList(reviewImages.getOrDefault(r.getId(), List.of()));
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
        dto.setHinhAnhList(reviewImageRepository.findByReviewIdOrderBySortOrderAsc(review.getId())
                .stream().map(ReviewImage::getImageUrl).toList());

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
    public ReviewDTO createReview(Integer userId, ReviewRequestDTO request, List<String> hinhAnhUrls) {
        if (hasReviewed(userId, request.getProductId())) {
            cleanupFiles(hinhAnhUrls);
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }
        if (!hasCompletedOrderAndPurchased(userId, request.getProductId())) {
            cleanupFiles(hinhAnhUrls);
            throw new RuntimeException("Bạn cần mua sản phẩm và thanh toán để được đánh giá");
        }
        if (request.getDanhGia() == null) {
            cleanupFiles(hinhAnhUrls);
            throw new RuntimeException("Vui lòng chọn số sao đánh giá");
        }

        Review review = new Review();
        review.setProductId(request.getProductId());
        review.setUserId(userId);
        review.setDanhGia(request.getDanhGia());
        review.setBinhLuan(HtmlSanitizer.sanitize(request.getBinhLuan()));
        review.setIsApproved(false);

        try {
            review = reviewsRepository.save(review);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Rang buoc UNIQUE (userId, productId) o DB — lop bao ve cuoi cung chong gui
            // trung khi khach double-click nut gui danh gia (check hasReviewed() o tren
            // la doc-roi-ghi, khong nguyen tu, van co the bi 2 request vuot qua gan nhu
            // cung luc truoc khi ben nao commit).
            cleanupFiles(hinhAnhUrls);
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        } catch (Exception e) {
            cleanupFiles(hinhAnhUrls);
            throw new RuntimeException("Lỗi khi lưu đánh giá", e);
        }

        // Save review images
        if (hinhAnhUrls != null) {
            int order = 0;
            for (String url : hinhAnhUrls) {
                if (url != null && !url.isBlank()) {
                    ReviewImage img = new ReviewImage();
                    img.setReviewId(review.getId());
                    img.setImageUrl(url);
                    img.setSortOrder(order++);
                    reviewImageRepository.save(img);
                }
            }
        }

        // Email thong bao cho admin — gui BAT DONG BO (best-effort, khong chan gui review)
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                asyncEmailService.sendRaw(
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

    public Map<Integer, Long> getRatingDistribution(Integer productId) {
        List<Object[]> rows = reviewsRepository.getRatingDistribution(productId);
        Map<Integer, Long> dist = new HashMap<>();
        for (int i = 1; i <= 5; i++) dist.put(i, 0L);
        for (Object[] row : rows) {
            dist.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return dist;
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getRecentReviewsByUser(Integer userId, int limit) {
        return reviewsRepository.findByUserIdOrderByNgayTaoDesc(userId)
                .stream().limit(limit).map(this::toDTO).toList();
    }

    private void cleanupFiles(List<String> urls) {
        if (urls != null) {
            for (String url : urls) {
                if (url != null) {
                    try {
                        fileUploadService.delete(url, "reviews");
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }
}
