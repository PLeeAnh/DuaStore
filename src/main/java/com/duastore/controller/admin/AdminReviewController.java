package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Product;
import com.duastore.model.Review;
import com.duastore.model.ReviewReply;
import com.duastore.repository.ContactMessageRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ReviewReplyRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminReviewService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/danh-gia")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới đánh giá sản phẩm.
 */
public class AdminReviewController {

    private final AdminReviewService adminReviewService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final NotificationHelper notificationHelper;
    private final ReviewReplyRepository reviewReplyRepository;
    private final SecurityUtil securityUtil;
    private final ContactMessageRepository contactMessageRepository;

    public AdminReviewController(AdminReviewService adminReviewService,
            ProductRepository productRepository,
            UserRepository userRepository,
            NotificationHelper notificationHelper,
            ReviewReplyRepository reviewReplyRepository,
            SecurityUtil securityUtil,
            ContactMessageRepository contactMessageRepository) {
        this.adminReviewService = adminReviewService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationHelper = notificationHelper;
        this.reviewReplyRepository = reviewReplyRepository;
        this.securityUtil = securityUtil;
        this.contactMessageRepository = contactMessageRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) String filter,
            Model model) {
        if (keyword != null && keyword.isBlank()) keyword = null;
        Integer maxScore = "low".equals(filter) ? 2 : null;
        boolean unanswered = "unanswered".equals(filter);
        Boolean approvedFilter = "hidden".equals(filter) ? Boolean.FALSE : isApproved;

        boolean hasFilter = keyword != null || approvedFilter != null || maxScore != null || unanswered;
        Page<Review> reviewPage = hasFilter
                ? adminReviewService.searchAdmin(keyword, approvedFilter, maxScore, unanswered, page, size)
                : adminReviewService.getAllReviews(page, size);

        Map<Integer, String> productNames = new HashMap<>();
        Map<Integer, String> userNames = new HashMap<>();
        Map<Integer, String> userInitials = new HashMap<>();
        Set<Integer> productIds = reviewPage.getContent().stream().map(Review::getProductId).collect(Collectors.toSet());
        Set<Integer> userIds = reviewPage.getContent().stream().map(Review::getUserId).collect(Collectors.toSet());
        productRepository.findAllById(productIds).forEach(p -> productNames.put(p.getId(), p.getTenSanPham()));
        userRepository.findAllById(userIds).forEach(u -> {
            userNames.put(u.getId(), u.getHoTen());
            userInitials.put(u.getId(), initialsOf(u.getHoTen()));
        });

        // Đếm reply cho mỗi review
        Map<Integer, Long> replyCountMap = new HashMap<>();
        Map<Integer, List<ReviewReply>> repliesMap = new HashMap<>();
        for (Review r : reviewPage.getContent()) {
            List<ReviewReply> replies = reviewReplyRepository.findByReviewIdOrderByCreatedAtAsc(r.getId());
            repliesMap.put(r.getId(), replies);
            replyCountMap.put(r.getId(), (long) replies.size());
        }

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("userNames", userNames);
        model.addAttribute("userInitials", userInitials);
        model.addAttribute("replyCountMap", replyCountMap);
        model.addAttribute("repliesMap", repliesMap);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("totalItems", reviewPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "đánh giá");
        model.addAttribute("url", "/admin/danh-gia");

        Map<String, Object> filterParams = new HashMap<>();
        if (keyword != null) filterParams.put("keyword", keyword);
        if (isApproved != null) filterParams.put("isApproved", isApproved);
        if (filter != null) filterParams.put("filter", filter);
        model.addAttribute("filterParams", filterParams);

        model.addAttribute("keyword", keyword);
        model.addAttribute("activeFilter", filter);

        // Stat cards
        model.addAttribute("avgRating", adminReviewService.getOverallAverageRating());
        model.addAttribute("totalReviewCount", reviewsCountAll());
        model.addAttribute("unansweredCount", adminReviewService.countUnanswered());
        model.addAttribute("lowRatingCount", adminReviewService.countLowRating());
        model.addAttribute("hiddenCount", adminReviewService.countHidden());
        model.addAttribute("contactMessageCount", contactMessageRepository.countByIsResolvedFalseAndIsSpamFalse());
        model.addAttribute("contactUnreadCount", contactMessageRepository.countByIsRead(false));

        model.addAttribute("title", "danh-gia");
        return "view/admin/review/review-list";
    }

    private long reviewsCountAll() {
        return adminReviewService.getAllReviews(0, 1).getTotalElements();
    }

    private String initialsOf(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "?";
        String[] parts = hoTen.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    @PostMapping("/duyet/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_APPROVE)")
    public String approve(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.approveReview(id);
            Review review = adminReviewService.getReviewById(id);
            notifyReviewAuthor(review, true);
            ra.addFlashAttribute("successMsg", "Đã duyệt đánh giá");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }

    @PostMapping("/an/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_HIDE)")
    public String reject(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.rejectReview(id);
            Review review = adminReviewService.getReviewById(id);
            notifyReviewAuthor(review, false);
            ra.addFlashAttribute("successMsg", "Đã ẩn đánh giá");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.deleteReview(id);
            ra.addFlashAttribute("successMsg", "Xóa đánh giá thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }

    /** API phản hồi đánh giá — AJAX */
    @PostMapping("/{id}/tra-loi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_APPROVE)")
    @ResponseBody
    public ResponseEntity<?> reply(@PathVariable Integer id, @RequestParam String content) {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nội dung không được để trống"));
        }
        Review review = adminReviewService.getReviewById(id);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        Integer adminId = securityUtil.getCurrentUserId();
        String adminName = securityUtil.getCurrentUser().getHoTen();

        ReviewReply reply = ReviewReply.builder()
                .reviewId(id)
                .content(content.trim())
                .createdBy(adminId)
                .build();
        reviewReplyRepository.save(reply);

        Map<String, Object> result = new HashMap<>();
        result.put("id", reply.getId());
        result.put("content", reply.getContent());
        result.put("adminName", adminName);
        result.put("createdAt", reply.getCreatedAt().toString());
        return ResponseEntity.ok(result);
    }

    private void notifyReviewAuthor(Review review, boolean approved) {
        String productName = productRepository.findById(review.getProductId())
                .map(Product::getTenSanPham)
                .orElse("san pham #" + review.getProductId());
        String action = approved ? "da duoc duyet" : "da bi an";
        notificationHelper.notifyAll(
                "Danh gia cua ban cho san pham " + productName + " " + action,
                "PRODUCT", review.getProductId(),
                "/san-pham/" + review.getProductId(),
                "Xem san pham",
                review.getUserId()
        );
        notificationHelper.notifyStaff(
                "Danh gia cho san pham " + productName + " " + action,
                "PRODUCT", review.getProductId(),
                "/admin/danh-gia",
                "Xem danh gia"
        );
    }
}
