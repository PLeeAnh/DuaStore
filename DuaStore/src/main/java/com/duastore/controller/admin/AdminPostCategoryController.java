package com.duastore.controller.admin;

import com.duastore.model.PostCategory;
import com.duastore.config.security.PermissionEnum;
import com.duastore.service.admin.AdminPostCategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/danh-muc-bai-viet")
public class AdminPostCategoryController {

    private final AdminPostCategoryService adminPostCategoryService;

    public AdminPostCategoryController(AdminPostCategoryService adminPostCategoryService) {
        this.adminPostCategoryService = adminPostCategoryService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_READ)")
    public String list(Model model) {
        model.addAttribute("title", "danh-muc-bai-viet");
        model.addAttribute("categories", adminPostCategoryService.getAll());
        return "view/admin/post-category/list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "danh-muc-bai-viet");
        model.addAttribute("category", new PostCategory());
        model.addAttribute("formAction", "/admin/danh-muc-bai-viet/them-moi");
        return "view/admin/post-category/form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_CREATE)")
    public String create(@Valid @ModelAttribute("category") PostCategory category,
                         BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc-bai-viet");
            model.addAttribute("formAction", "/admin/danh-muc-bai-viet/them-moi");
            return "view/admin/post-category/form";
        }
        try {
            adminPostCategoryService.save(category);
            ra.addFlashAttribute("successMsg", "Thêm danh mục bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-muc-bai-viet";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            PostCategory category = adminPostCategoryService.getById(id);
            model.addAttribute("title", "danh-muc-bai-viet");
            model.addAttribute("category", category);
            model.addAttribute("formAction", "/admin/danh-muc-bai-viet/sua/" + id);
            return "view/admin/post-category/form";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/danh-muc-bai-viet";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("category") PostCategory category,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc-bai-viet");
            model.addAttribute("formAction", "/admin/danh-muc-bai-viet/sua/" + id);
            return "view/admin/post-category/form";
        }
        try {
            category.setId(id);
            adminPostCategoryService.save(category);
            ra.addFlashAttribute("successMsg", "Cập nhật danh mục bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-muc-bai-viet";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).POST_CATEGORY_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminPostCategoryService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa danh mục bài viết thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/danh-muc-bai-viet";
    }
}
