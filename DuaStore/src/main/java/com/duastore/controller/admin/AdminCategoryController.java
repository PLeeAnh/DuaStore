package com.duastore.controller.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.model.Category;
import com.duastore.service.admin.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/danh-muc")
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    public AdminCategoryController(AdminCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        model.addAttribute("title", "danh-muc");
        Page<Category> categoryPage = categoryService.findAllPaged(page, size);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categoryPage.getTotalPages());
        model.addAttribute("totalItems", categoryPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "danh mục");
        model.addAttribute("url", "/admin/danh-muc");
        model.addAttribute("filterParams", new java.util.HashMap<>());
        return "view/admin/category/category-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("parents", categoryService.findAvailableParents(null));
        return "view/admin/category/category-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_CREATE)")
    public String create(@Valid @ModelAttribute("category") CategoryDTO dto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(null));
            return "view/admin/category/category-form";
        }
        categoryService.save(dto);
        ra.addFlashAttribute("successMsg", "Thêm danh mục thành công");
        return "redirect:/admin/danh-muc";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Category category = categoryService.findById(id);
        if (category == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/danh-muc";
        }
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", categoryService.toDto(category));
        model.addAttribute("parents", categoryService.findAvailableParents(id));
        return "view/admin/category/category-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("category") CategoryDTO dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(id));
            return "view/admin/category/category-form";
        }
        dto.setId(id);
        categoryService.save(dto);
        ra.addFlashAttribute("successMsg", "Cập nhật danh mục thành công");
        return "redirect:/admin/danh-muc";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (!categoryService.softDelete(id)) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/danh-muc";
        }
        ra.addFlashAttribute("successMsg", "Đã ẩn danh mục");
        return "redirect:/admin/danh-muc";
    }
}
