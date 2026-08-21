package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Promotion;
import com.duastore.model.User;
import com.duastore.repository.PromotionRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.CategoryRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminPromotionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/admin/khuyen-mai")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới khuyến mãi.
 */
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;
    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationHelper notificationHelper;
    private final SecurityUtil securityUtil;

    public AdminPromotionController(AdminPromotionService adminPromotionService,
            PromotionRepository promotionRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            NotificationHelper notificationHelper,
            SecurityUtil securityUtil) {
        this.adminPromotionService = adminPromotionService;
        this.promotionRepository = promotionRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.notificationHelper = notificationHelper;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute
    public void addTargetModels(Model model) {
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            Model model) {
        Page<Promotion> promoPage;
        if (keyword != null || isActive != null) {
            promoPage = adminPromotionService.searchPromotions(keyword, isActive, page, size);
        } else {
            promoPage = adminPromotionService.getAllPromotionsWithExpiry(page, size);
        }
        model.addAttribute("promotions", promoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promoPage.getTotalPages());
        model.addAttribute("totalItems", promoPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "khuyến mãi");
        model.addAttribute("url", "/admin/khuyen-mai");
        java.util.Map<String, Object> filterParams = new java.util.HashMap<>();
        if (keyword != null) {
            filterParams.put("keyword", keyword);
        }
        if (isActive != null) {
            filterParams.put("isActive", isActive);
        }
        model.addAttribute("filterParams", filterParams);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isActive", isActive);
        model.addAttribute("title", "khuyen-mai");
        model.addAttribute("promotionTab", "khuyen-mai");
        model.addAttribute("totalPromotions", promotionRepository.count());
        model.addAttribute("activePromotionsCount", promotionRepository.countByIsActiveTrue());
        model.addAttribute("inactivePromotionsCount", promotionRepository.countByIsActiveFalse());
        model.addAttribute("usageCounts", adminPromotionService.getUsageCounts());
        return "view/admin/promotion/promotion-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
        model.addAttribute("title", "khuyen-mai");
        return "view/admin/promotion/promotion-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_CREATE)")
    public String create(@Valid @ModelAttribute Promotion promotion, BindingResult result,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            return "view/admin/promotion/promotion-form";
        }
        try {
            if (tuNgay != null && !tuNgay.isBlank()) {
                promotion.setTuNgay(LocalDateTime.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
            if (denNgay != null && !denNgay.isBlank()) {
                promotion.setDenNgay(LocalDateTime.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            model.addAttribute("errorMsg", "Định dạng ngày không hợp lệ");
            return "view/admin/promotion/promotion-form";
        }
        if (promotion.getGiaTriGiam() != null && promotion.getGiaTriGiam().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            model.addAttribute("errorMsg", "Giá trị giảm phải lớn hơn 0");
            return "view/admin/promotion/promotion-form";
        }
        if (!StringUtils.hasText(promotion.getTenChuongTrinh()) || promotion.getTenChuongTrinh().length() > 200) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            model.addAttribute("errorMsg", "Tên chương trình không hợp lệ");
            return "view/admin/promotion/promotion-form";
        }
        if (!StringUtils.hasText(promotion.getMaCode()) || promotion.getMaCode().length() > 50) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            model.addAttribute("errorMsg", "Mã code không hợp lệ");
            return "view/admin/promotion/promotion-form";
        }
        try {
            promotion.setId(null);
            Promotion saved = adminPromotionService.savePromotion(promotion);
            notificationHelper.notifyAll(
                    "🔥 Khuyến mãi mới: " + saved.getTenChuongTrinh() + " — giảm " + formatDiscount(saved),
                    "PROMOTION", saved.getId(),
                    "/khuyen-mai",
                    "Xem khuyến mãi"
            );
            notificationHelper.notifyStaff(
                    "Admin " + getCurrentAdminName() + " đã tạo khuyến mãi: " + saved.getTenChuongTrinh(),
                    "PROMOTION", saved.getId(),
                    "/admin/khuyen-mai",
                    "Xem khuyến mãi"
            );
            ra.addFlashAttribute("successMsg", "Thêm khuyến mãi thành công");
        } catch (Exception e) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
            model.addAttribute("errorMsg", e.getMessage());
            return "view/admin/promotion/promotion-form";
        }
        return "redirect:/admin/khuyen-mai";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            Promotion p = adminPromotionService.getPromotionById(id);
            model.addAttribute("promotion", p);
            model.addAttribute("formAction", "/admin/khuyen-mai/sua/" + id);
            model.addAttribute("title", "khuyen-mai");
            return "view/admin/promotion/promotion-form";
        } catch (Exception e) {
            return "redirect:/admin/khuyen-mai";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute Promotion promotion, BindingResult result,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/sua/" + id);
            return "view/admin/promotion/promotion-form";
        }
        try {
            if (tuNgay != null && !tuNgay.isBlank()) {
                promotion.setTuNgay(LocalDateTime.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
            if (denNgay != null && !denNgay.isBlank()) {
                promotion.setDenNgay(LocalDateTime.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/sua/" + id);
            model.addAttribute("errorMsg", "Định dạng ngày không hợp lệ");
            return "view/admin/promotion/promotion-form";
        }
        if (promotion.getGiaTriGiam() != null && promotion.getGiaTriGiam().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/sua/" + id);
            model.addAttribute("errorMsg", "Giá trị giảm phải lớn hơn 0");
            return "view/admin/promotion/promotion-form";
        }
        try {
            promotion.setId(id);
            adminPromotionService.savePromotion(promotion);
            ra.addFlashAttribute("successMsg", "Cập nhật khuyến mãi thành công");
        } catch (Exception e) {
            model.addAttribute("title", "khuyen-mai");
            model.addAttribute("promotion", promotion);
            model.addAttribute("formAction", "/admin/khuyen-mai/sua/" + id);
            model.addAttribute("errorMsg", e.getMessage());
            return "view/admin/promotion/promotion-form";
        }
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPromotionService.deletePromotion(id);
            ra.addFlashAttribute("successMsg", "Xóa khuyến mãi thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PROMOTION_UPDATE)")
    public String toggleActive(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPromotionService.toggleActive(id);
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }
    private String formatDiscount(Promotion promotion) {
        if ("PERCENT".equalsIgnoreCase(promotion.getLoaiGiam())
                || "PHAN_TRAM".equalsIgnoreCase(promotion.getLoaiGiam())) {
            return promotion.getGiaTriGiam().stripTrailingZeros().toPlainString() + "%";
        }
        return promotion.getGiaTriGiam().stripTrailingZeros().toPlainString() + " VND";
    }

    private String getCurrentAdminName() {
        try {
            User admin = securityUtil.getCurrentUser();
            return admin != null ? admin.getHoTen() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
