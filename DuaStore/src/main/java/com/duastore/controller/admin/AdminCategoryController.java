package com.duastore.controller.admin;

import com.duastore.dto.CategoryDTO;
import com.duastore.model.Category;
import com.duastore.service.FileUploadService;
import com.duastore.service.admin.AdminCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/danh-muc")
public class AdminCategoryController {

    private final AdminCategoryService categoryService;
    private final FileUploadService fileUploadService;

    public AdminCategoryController(AdminCategoryService categoryService, FileUploadService fileUploadService) {
        this.categoryService = categoryService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_READ)")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       Model model) {
        model.addAttribute("title", "danh-muc");

        boolean searching = (keyword != null && !keyword.isBlank())
                || (status != null && !status.isBlank());

        List<Category> all = categoryService.findAll();
        model.addAttribute("totalCategories", all.size());
        model.addAttribute("rootCategories", categoryService.countRootCategories());
        model.addAttribute("childCategories", categoryService.countChildCategories());
        model.addAttribute("activeCategories", all.stream().filter(Category::isActive).count());

        Map<Integer, Long> productCountMap = categoryService.getProductCountMap();
        model.addAttribute("productCountMap", productCountMap);

        if (searching) {
            List<Category> results = categoryService.search(keyword, status);
            model.addAttribute("searchResults", results);
            model.addAttribute("showSearchResult", true);
        } else {
            model.addAttribute("flatTree", categoryService.getFlatTree(productCountMap));
            model.addAttribute("showSearchResult", false);
        }

        return "view/admin/category/category-list";
    }

    @GetMapping("/chi-tiet/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_READ)")
    public String detail(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        CategoryDTO dto = categoryService.findByIdAsDto(id);
        if (dto == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/danh-muc";
        }
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", dto);

        List<Category> children = categoryService.findChildrenByParentId(id);
        model.addAttribute("children", children);

        Map<Integer, Long> productCountMap = categoryService.getProductCountMap();
        model.addAttribute("productCountMap", productCountMap);
        model.addAttribute("productCount", productCountMap.getOrDefault(id, 0L));

        return "view/admin/category/category-detail";
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
                         @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(null));
            return "view/admin/category/category-form";
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String url = fileUploadService.save(imageFile, "categories");
            dto.setImageUrl(url);
        }
        categoryService.save(dto);
        ra.addFlashAttribute("successMsg", "Thêm danh mục thành công");
        return "redirect:/admin/danh-muc";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        CategoryDTO dto = categoryService.findByIdAsDto(id);
        if (dto == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/danh-muc";
        }
        model.addAttribute("title", "danh-muc");
        model.addAttribute("category", dto);
        model.addAttribute("parents", categoryService.findAvailableParents(id));
        return "view/admin/category/category-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @Valid @ModelAttribute("category") CategoryDTO dto,
                       BindingResult result,
                       @RequestParam(name = "imageFile", required = false) MultipartFile imageFile,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "danh-muc");
            model.addAttribute("parents", categoryService.findAvailableParents(id));
            return "view/admin/category/category-form";
        }
        dto.setId(id);

        Category existing = categoryService.findById(id);
        if (existing != null) {
            dto.setImageUrl(existing.getImageUrl());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String url = fileUploadService.save(imageFile, "categories");
            dto.setImageUrl(url);
        }

        categoryService.save(dto);
        ra.addFlashAttribute("successMsg", "Cập nhật danh mục thành công");
        return "redirect:/admin/danh-muc";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (categoryService.hasChildren(id)) {
            ra.addFlashAttribute("errorMsg", "Vui lòng xóa danh mục con trước.");
            return "redirect:/admin/danh-muc";
        }
        if (categoryService.hasProducts(id)) {
            ra.addFlashAttribute("errorMsg", "Danh mục đang chứa sản phẩm. Không thể xóa.");
            return "redirect:/admin/danh-muc";
        }
        if (!categoryService.softDelete(id)) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy danh mục");
            return "redirect:/admin/danh-muc";
        }
        ra.addFlashAttribute("successMsg", "Đã ẩn danh mục");
        return "redirect:/admin/danh-muc";
    }

    @PostMapping("/{id}/xoa-anh")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).CATEGORY_UPDATE)")
    public ResponseEntity<?> deleteImage(@PathVariable Integer id) {
        if (categoryService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        categoryService.clearImageUrl(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
