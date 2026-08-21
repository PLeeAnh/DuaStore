package com.duastore.controller.admin;

import com.duastore.dto.PopupBannerFormDTO;
import com.duastore.model.PopupBanner;
import com.duastore.service.admin.AdminPopupBannerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/popup-banner")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới banner popup, banner quảng cáo.
 */
public class AdminPopupBannerController {

    private final AdminPopupBannerService adminPopupBannerService;

    public AdminPopupBannerController(AdminPopupBannerService adminPopupBannerService) {
        this.adminPopupBannerService = adminPopupBannerService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_READ)")
    public String list(Model model) {
        model.addAttribute("title", "popup-banner");
        model.addAttribute("banners", adminPopupBannerService.getAll());
        return "view/admin/popup-banner/list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_CREATE)")
    public String createForm(Model model) {
        PopupBannerFormDTO form = new PopupBannerFormDTO();
        form.setActive(true);
        prepareForm(model, form, "/admin/popup-banner/them-moi");
        return "view/admin/popup-banner/form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_CREATE)")
    public String create(@Valid @ModelAttribute("popupBanner") PopupBannerFormDTO form,
                         BindingResult result, Model model, RedirectAttributes ra) {
        validateImage(form, result);
        if (result.hasErrors()) {
            prepareForm(model, form, "/admin/popup-banner/them-moi");
            return "view/admin/popup-banner/form";
        }
        try {
            adminPopupBannerService.save(form);
            ra.addFlashAttribute("successMsg", "Thêm popup banner thành công");
            return "redirect:/admin/popup-banner";
        } catch (Exception e) {
            result.reject("popup.save", e.getMessage());
            prepareForm(model, form, "/admin/popup-banner/them-moi");
            return "view/admin/popup-banner/form";
        }
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            PopupBanner banner = adminPopupBannerService.getById(id);
            PopupBannerFormDTO form = toForm(banner);
            prepareForm(model, form, "/admin/popup-banner/sua/" + id);
            return "view/admin/popup-banner/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/popup-banner";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("popupBanner") PopupBannerFormDTO form,
                       BindingResult result, Model model, RedirectAttributes ra) {
        form.setId(id);
        if (result.hasErrors()) {
            prepareForm(model, form, "/admin/popup-banner/sua/" + id);
            return "view/admin/popup-banner/form";
        }
        try {
            adminPopupBannerService.save(form);
            ra.addFlashAttribute("successMsg", "Cập nhật popup banner thành công");
            return "redirect:/admin/popup-banner";
        } catch (Exception e) {
            result.reject("popup.save", e.getMessage());
            prepareForm(model, form, "/admin/popup-banner/sua/" + id);
            return "view/admin/popup-banner/form";
        }
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPopupBannerService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa popup banner thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/popup-banner";
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).BANNER_UPDATE)")
    public String toggle(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            PopupBanner banner = adminPopupBannerService.toggleActive(id);
            ra.addFlashAttribute("successMsg",
                    Boolean.TRUE.equals(banner.getActive()) ? "Đã bật popup" : "Đã tắt popup");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/popup-banner";
    }

    private void validateImage(PopupBannerFormDTO form, BindingResult result) {
        if (form.getId() == null && (form.getImageFile() == null || form.getImageFile().isEmpty())) {
            result.rejectValue("imageFile", "popup.image.required", "Vui lòng chọn ảnh popup");
        }
    }

    private void prepareForm(Model model, PopupBannerFormDTO form, String action) {
        model.addAttribute("title", "popup-banner");
        model.addAttribute("popupBanner", form);
        model.addAttribute("formAction", action);
    }

    private PopupBannerFormDTO toForm(PopupBanner banner) {
        PopupBannerFormDTO form = new PopupBannerFormDTO();
        form.setId(banner.getId());
        form.setTitle(banner.getTitle());
        form.setImageUrl(banner.getImageUrl());
        form.setLinkUrl(banner.getLinkUrl());
        form.setDisplayMode(banner.getDisplayMode());
        form.setIntervalMinutes(banner.getIntervalMinutes());
        form.setActive(banner.getActive());
        return form;
    }
}
