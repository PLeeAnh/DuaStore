package com.duastore.controller.admin;

import com.duastore.dto.ProductVariantFormDTO;
import com.duastore.service.admin.AdminVariantService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/bien-the")
public class AdminVariantController {

    private final AdminVariantService variantService;

    public AdminVariantController(AdminVariantService variantService) {
        this.variantService = variantService;
    }

    @GetMapping("/them-moi/{productId}")
    public String createForm(@PathVariable Integer productId, Model model) {
        ProductVariantFormDTO dto = new ProductVariantFormDTO();
        dto.setProductId(productId);
        model.addAttribute("variant", dto);
        model.addAttribute("title", "san-pham");
        return "view/admin/productvariant/variant-form";
    }

    @PostMapping("/them-moi")
    public String create(@Valid ProductVariantFormDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            return "view/admin/productvariant/variant-form";
        }
        var saved = variantService.save(dto);
        return "redirect:/admin/san-pham/" + saved.getProductId() + "/bien-the?successMsg=Them+bien+the+thanh+cong";
    }

    @GetMapping("/sua/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        var v = variantService.findById(id);
        if (v == null) return "redirect:/admin/san-pham?errorMsg=Khong+tim+thay+bien+the";

        ProductVariantFormDTO dto = new ProductVariantFormDTO();
        dto.setId(v.getId());
        dto.setProductId(v.getProductId());
        dto.setTenBienThe(v.getTenBienThe());
        dto.setDungTich(v.getDungTich());
        dto.setGiaGoc(v.getGiaGoc());
        dto.setGiaKhuyenMai(v.getGiaKhuyenMai());
        dto.setSoLuongTon(v.getSoLuongTon());
        dto.setHinhAnh(v.getHinhAnh());
        dto.setDefault(v.isDefault());

        model.addAttribute("variant", dto);
        model.addAttribute("title", "san-pham");
        return "view/admin/productvariant/variant-form";
    }

    @PostMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, @Valid ProductVariantFormDTO dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            return "view/admin/productvariant/variant-form";
        }
        dto.setId(id);
        var saved = variantService.save(dto);
        return "redirect:/admin/san-pham/" + saved.getProductId() + "/bien-the?successMsg=Cap+nhat+bien+the+thanh+cong";
    }

    @PostMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        var v = variantService.findById(id);
        if (v == null) return "redirect:/admin/san-pham?errorMsg=Khong+tim+thay+bien+the";
        int productId = v.getProductId();
        variantService.delete(id);
        return "redirect:/admin/san-pham/" + productId + "/bien-the?successMsg=Da+xoa+bien+the";
    }
}
