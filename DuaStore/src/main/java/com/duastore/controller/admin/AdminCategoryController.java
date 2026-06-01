package com.duastore.controller.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.model.Category;
import com.duastore.service.admin.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/danh-muc")
public class AdminCategoryController {

    private final AdminCategoryService categoryService;

    public AdminCategoryController(AdminCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("title", "danh-muc");
        model.addAttribute("categories", categoryService.findAll());
        return "view/admin/category/category-list";
    }

    @GetMapping("/them-moi")
    public String createForm(Model model) {
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("parents", categoryService.findAvailableParents(null));
        return "view/admin/category/category-form";
    }

    @PostMapping("/them-moi")
    public String create(@Valid @ModelAttribute("category") CategoryDTO dto,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(null));
            return "view/admin/category/category-form";
        }
        categoryService.save(dto);
        return "redirect:/admin/danh-muc?successMsg=Them+danh+muc+thanh+cong";
    }

    @GetMapping("/sua/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Category category = categoryService.findById(id);
        if (category == null) {
            return "redirect:/admin/danh-muc?errorMsg=Khong+tim+thay+danh+muc";
        }
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", categoryService.toDto(category));
        model.addAttribute("parents", categoryService.findAvailableParents(id));
        return "view/admin/category/category-form";
    }

    @PostMapping("/sua/{id}")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("category") CategoryDTO dto,
                       BindingResult result,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(id));
            return "view/admin/category/category-form";
        }
        dto.setId(id);
        categoryService.save(dto);
        return "redirect:/admin/danh-muc?successMsg=Cap+nhat+danh+muc+thanh+cong";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        if (!categoryService.softDelete(id)) {
            return "redirect:/admin/danh-muc?errorMsg=Khong+tim+thay+danh+muc";
        }
        return "redirect:/admin/danh-muc?successMsg=Da+an+danh+muc";
    }
}
