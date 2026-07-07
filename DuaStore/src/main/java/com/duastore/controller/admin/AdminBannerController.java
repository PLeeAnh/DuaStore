package com.duastore.controller.admin;

import com.duastore.dto.BannerFormDTO;
import com.duastore.model.Banner;
import com.duastore.service.BannerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/banner")
public class AdminBannerController {

    private final BannerService bannerService;

    public AdminBannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_READ)")
    public String list(Model model) {
        model.addAttribute("title", "banner");
        model.addAttribute("banners", bannerService.getAllForAdmin());
        model.addAttribute("now", LocalDateTime.now());
        return "view/admin/banner/list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_CREATE)")
    public String createForm(Model model) {
        BannerFormDTO form = new BannerFormDTO();
        form.setActive(true);
        prepareForm(model, form, "/admin/banner/them-moi");
        return "view/admin/banner/form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_CREATE)")
    public String create(@Valid @ModelAttribute("banner") BannerFormDTO form,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        validateImage(form, result);
        if (result.hasErrors()) {
            prepareForm(model, form, "/admin/banner/them-moi");
            return "view/admin/banner/form";
        }
        try {
            bannerService.save(form);
            redirectAttributes.addFlashAttribute("successMsg", "Thêm banner thành công");
            return "redirect:/admin/banner";
        } catch (RuntimeException ex) {
            result.reject("banner.save", ex.getMessage());
            prepareForm(model, form, "/admin/banner/them-moi");
            return "view/admin/banner/form";
        }
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Banner banner = bannerService.getById(id);
            BannerFormDTO form = toForm(banner);
            prepareForm(model, form, "/admin/banner/sua/" + id);
            return "view/admin/banner/form";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
            return "redirect:/admin/banner";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("banner") BannerFormDTO form,
                       BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        form.setId(id);
        if (result.hasErrors()) {
            prepareForm(model, form, "/admin/banner/sua/" + id);
            return "view/admin/banner/form";
        }
        try {
            bannerService.save(form);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật banner thành công");
            return "redirect:/admin/banner";
        } catch (RuntimeException ex) {
            result.reject("banner.save", ex.getMessage());
            prepareForm(model, form, "/admin/banner/sua/" + id);
            return "view/admin/banner/form";
        }
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            bannerService.delete(id);
            redirectAttributes.addFlashAttribute("successMsg", "Xóa banner thành công");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/admin/banner";
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String toggle(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            Banner banner = bannerService.toggleActive(id);
            redirectAttributes.addFlashAttribute("successMsg",
                    Boolean.TRUE.equals(banner.getActive()) ? "Đã bật banner" : "Đã tắt banner");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        }
        return "redirect:/admin/banner";
    }

    private void validateImage(BannerFormDTO form, BindingResult result) {
        if (form.getImageFile() == null || form.getImageFile().isEmpty()) {
            result.rejectValue("imageFile", "banner.image.required", "Vui lòng chọn ảnh banner");
        }
    }

    private void prepareForm(Model model, BannerFormDTO form, String action) {
        model.addAttribute("title", "banner");
        model.addAttribute("banner", form);
        model.addAttribute("formAction", action);
    }

    private BannerFormDTO toForm(Banner banner) {
        BannerFormDTO form = new BannerFormDTO();
        form.setId(banner.getId());
        form.setTitle(banner.getTitle());
        form.setImageUrl(banner.getImageUrl());
        form.setLinkUrl(banner.getLinkUrl());
        form.setActive(banner.getActive());
        form.setDisplayOrder(banner.getDisplayOrder());
        form.setStartDate(banner.getStartDate());
        form.setEndDate(banner.getEndDate());
        form.setDescription(banner.getDescription());
        return form;
    }
}
