package com.duastore.controller.admin;

import com.duastore.model.Promotion;
import com.duastore.service.admin.AdminPromotionService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String create(@ModelAttribute Promotion promotion, RedirectAttributes ra) {
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
    public String edit(@PathVariable Integer id, @ModelAttribute Promotion promotion, RedirectAttributes ra) {
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
