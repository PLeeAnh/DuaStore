package com.duastore.controller.admin;

import com.duastore.model.Promotion;
import com.duastore.service.admin.AdminPromotionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

@Controller
@RequestMapping("/admin/khuyen-mai")
public class AdminPromotionController {

    private final AdminPromotionService adminPromotionService;

    public AdminPromotionController(AdminPromotionService adminPromotionService) {
        this.adminPromotionService = adminPromotionService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Promotion> promoPage = adminPromotionService.getAllPromotions(page, 20);
        model.addAttribute("promotions", promoPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promoPage.getTotalPages());
        model.addAttribute("title", "khuyen-mai");
        return "view/admin/promotion/promotion-list";
    }

    @GetMapping("/them-moi")
    public String createForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("formAction", "/admin/khuyen-mai/them-moi");
        model.addAttribute("title", "khuyen-mai");
        return "view/admin/promotion/promotion-form";
    }

    @PostMapping("/them-moi")
    public String create(@Valid @ModelAttribute Promotion promotion, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Vui lòng kiểm tra lại thông tin khuyến mãi");
            return "redirect:/admin/khuyen-mai/them-moi";
        }
        if (promotion.getGiaTriGiam() != null && promotion.getGiaTriGiam().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Giá trị giảm phải lớn hơn 0");
            return "redirect:/admin/khuyen-mai/them-moi";
        }
        if (!StringUtils.hasText(promotion.getTenChuongTrinh()) || promotion.getTenChuongTrinh().length() > 200) {
            ra.addFlashAttribute("errorMsg", "Tên chương trình không hợp lệ");
            return "redirect:/admin/khuyen-mai/them-moi";
        }
        if (!StringUtils.hasText(promotion.getMaCode()) || promotion.getMaCode().length() > 50) {
            ra.addFlashAttribute("errorMsg", "Mã code không hợp lệ");
            return "redirect:/admin/khuyen-mai/them-moi";
        }
        try {
            adminPromotionService.savePromotion(promotion);
            ra.addFlashAttribute("successMsg", "Thêm khuyến mãi thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    @GetMapping("/sua/{id}")
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
    public String edit(@PathVariable Integer id, @Valid @ModelAttribute Promotion promotion, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg", "Vui lòng kiểm tra lại thông tin khuyến mãi");
            return "redirect:/admin/khuyen-mai/sua/" + id;
        }
        if (promotion.getGiaTriGiam() != null && promotion.getGiaTriGiam().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("errorMsg", "Giá trị giảm phải lớn hơn 0");
            return "redirect:/admin/khuyen-mai/sua/" + id;
        }
        try {
            promotion.setId(id);
            adminPromotionService.savePromotion(promotion);
            ra.addFlashAttribute("successMsg", "Cập nhật khuyến mãi thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPromotionService.deletePromotion(id);
            ra.addFlashAttribute("successMsg", "Xóa khuyến mãi thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/khuyen-mai";
    }
}
