package com.duastore.controller.admin;

import com.duastore.model.Review;
import com.duastore.service.admin.AdminReviewService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/danh-gia")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    public AdminReviewController(AdminReviewService adminReviewService) {
        this.adminReviewService = adminReviewService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Review> reviewPage = adminReviewService.getAllReviews(page, 20);

        Map<Integer, String> productNames = new HashMap<>();
        Map<Integer, String> userNames = new HashMap<>();
        for (Review r : reviewPage.getContent()) {
            productNames.put(r.getProductId(), adminReviewService.getTenSanPham(r.getProductId()));
            userNames.put(r.getUserId(), adminReviewService.getHoTenUser(r.getUserId()));
        }

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("userNames", userNames);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("title", "danh-gia");
        return "view/admin/review/review-list";
    }

    @PostMapping("/duyet/{id}")
    public String approve(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.approveReview(id);
            ra.addFlashAttribute("successMsg", "Đã duyệt đánh giá");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }

    @PostMapping("/an/{id}")
    public String reject(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.rejectReview(id);
            ra.addFlashAttribute("successMsg", "Đã ẩn đánh giá");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }

    @PostMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminReviewService.deleteReview(id);
            ra.addFlashAttribute("successMsg", "Xóa đánh giá thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-gia";
    }
}
