package com.duastore.controller.admin;

import com.duastore.model.Product;
import com.duastore.model.Review;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminReviewService;
import org.springframework.data.domain.Page;
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

    public AdminReviewController(AdminReviewService adminReviewService,
            ProductRepository productRepository,
            UserRepository userRepository,
            NotificationHelper notificationHelper) {
        this.adminReviewService = adminReviewService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.notificationHelper = notificationHelper;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REVIEW_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<Review> reviewPage = adminReviewService.getAllReviews(page, size);

        Map<Integer, String> productNames = new HashMap<>();
        Map<Integer, String> userNames = new HashMap<>();
        Set<Integer> productIds = reviewPage.getContent().stream().map(Review::getProductId).collect(Collectors.toSet());
        Set<Integer> userIds = reviewPage.getContent().stream().map(Review::getUserId).collect(Collectors.toSet());
        productRepository.findAllById(productIds).forEach(p -> productNames.put(p.getId(), p.getTenSanPham()));
        userRepository.findAllById(userIds).forEach(u -> userNames.put(u.getId(), u.getHoTen()));

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("userNames", userNames);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("totalItems", reviewPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "đánh giá");
        model.addAttribute("url", "/admin/danh-gia");
        model.addAttribute("filterParams", new HashMap<>());
        model.addAttribute("title", "danh-gia");
        return "view/admin/review/review-list";
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
