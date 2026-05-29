package com.duastore.controller.admin;

import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.repository.CategoryRepository;
import com.duastore.service.admin.AdminProductService;
import com.duastore.service.admin.AdminVariantService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/san-pham")
public class AdminProductController {

    private final AdminProductService productService;
    private final AdminVariantService variantService;
    private final CategoryRepository categoryRepository;

    public AdminProductController(AdminProductService productService,
                                   AdminVariantService variantService,
                                   CategoryRepository categoryRepository) {
        this.productService = productService;
        this.variantService = variantService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("title", "san-pham");

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (trangThai != null && trangThai.isBlank()) trangThai = null;

        boolean hasFilter = (keyword != null)
                         || danhMuc != null
                         || (trangThai != null);

        if (hasFilter) {
            model.addAttribute("products", productService.search(keyword, danhMuc, trangThai));
        } else {
            model.addAttribute("products", productService.findAll());
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("trangThai", trangThai);
        List<Category> cats = categoryRepository.findByIsActiveTrue();
        model.addAttribute("categories", cats);
        model.addAttribute("categoryMap", cats.stream().collect(Collectors.toMap(Category::getId, Category::getTenDanhMuc)));
        return "view/admin/product/product-list";
    }

    @GetMapping("/them-moi")
    public String createForm(Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("product", new ProductFormDTO());
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        return "view/admin/product/product-form";
    }

    @PostMapping("/them-moi")
    public String create(@Valid ProductFormDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            return "view/admin/product/product-form";
        }
        productService.save(dto);
        return "redirect:/admin/san-pham?successMsg=Them+san+pham+thanh+cong";
    }

    @GetMapping("/sua/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Product p = productService.findById(id);
        if (p == null) return "redirect:/admin/san-pham?errorMsg=Khong+tim+thay+san+pham";

        ProductFormDTO dto = new ProductFormDTO();
        dto.setId(p.getId());
        dto.setTenSanPham(p.getTenSanPham());
        dto.setMoTa(p.getMoTa());
        dto.setChatLieu(p.getChatLieu());
        dto.setXuatXu(p.getXuatXu());
        dto.setMucDichSuDung(p.getMucDichSuDung());
        dto.setThuongHieu(p.getThuongHieu());
        dto.setKinhLoai(p.getKinhLoai());
        dto.setDanhMucId(p.getDanhMucId());
        dto.setHinhAnhChinh(p.getHinhAnhChinh());
        dto.setTrangThaiSanPham(p.getTrangThaiSanPham());
        dto.setLeadTimeDays(p.getLeadTimeDays());
        dto.setFeatured(p.isFeatured());

        model.addAttribute("title", "san-pham");
        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        return "view/admin/product/product-form";
    }

    @PostMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, @Valid ProductFormDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            return "view/admin/product/product-form";
        }
        dto.setId(id);
        productService.save(dto);
        return "redirect:/admin/san-pham?successMsg=Cap+nhat+san+pham+thanh+cong";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        productService.delete(id);
        return "redirect:/admin/san-pham?successMsg=Da+xoa+san+pham";
    }

    // ── Variants ──

    @GetMapping("/{id}/bien-the")
    public String variantList(@PathVariable Integer id,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("product", productService.findById(id));
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("variants", variantService.searchByProductId(id, keyword));
        } else {
            model.addAttribute("variants", variantService.findByProductId(id));
        }
        model.addAttribute("keyword", keyword);
        return "view/admin/productvariant/variant-list";
    }
}
