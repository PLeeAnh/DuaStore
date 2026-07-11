package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.ProductVariantFormDTO;
import com.duastore.repository.ProductRepository;
import com.duastore.service.admin.AdminVariantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/bien-the")
public class AdminVariantController {

    private final AdminVariantService variantService;
    private final ProductRepository productRepository;
    private final SecurityUtil securityUtil;

    public AdminVariantController(AdminVariantService variantService, ProductRepository productRepository,
                                  SecurityUtil securityUtil) {
        this.variantService = variantService;
        this.productRepository = productRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/them-moi/{productId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_CREATE)")
    public String createForm(@PathVariable Integer productId, Model model) {
        ProductVariantFormDTO dto = new ProductVariantFormDTO();
        dto.setProductId(productId);
        model.addAttribute("variant", dto);
        model.addAttribute("title", "san-pham");
        var p = productRepository.findById(productId).orElse(null);
        model.addAttribute("productName", p != null ? p.getTenSanPham() : "—");
        model.addAttribute("capacities", variantService.getDistinctDungTich());
        return "view/admin/productvariant/variant-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_CREATE)")
    public String create(@Valid ProductVariantFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("variant", dto);
            model.addAttribute("title", "san-pham");
            var p = productRepository.findById(dto.getProductId()).orElse(null);
            model.addAttribute("productName", p != null ? p.getTenSanPham() : "—");
            model.addAttribute("capacities", variantService.getDistinctDungTich());
            return "view/admin/productvariant/variant-form";
        }
        var saved = variantService.save(dto);
        ra.addFlashAttribute("successMsg", "Thêm biến thể thành công");
        return "redirect:/admin/san-pham/chi-tiet/" + saved.getProductId();
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        var v = variantService.findById(id);
        if (v == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy biến thể");
            return "redirect:/admin/san-pham";
        }

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
        var p = productRepository.findById(v.getProductId()).orElse(null);
        model.addAttribute("productName", p != null ? p.getTenSanPham() : "—");
        model.addAttribute("capacities", variantService.getDistinctDungTich());
        return "view/admin/productvariant/variant-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid ProductVariantFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("variant", dto);
            model.addAttribute("title", "san-pham");
            var p = productRepository.findById(dto.getProductId()).orElse(null);
            model.addAttribute("productName", p != null ? p.getTenSanPham() : "—");
            model.addAttribute("capacities", variantService.getDistinctDungTich());
            return "view/admin/productvariant/variant-form";
        }
        dto.setId(id);
        var saved = variantService.save(dto);
        ra.addFlashAttribute("successMsg", "Cập nhật biến thể thành công");
        return "redirect:/admin/san-pham/chi-tiet/" + dto.getProductId();
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        var v = variantService.findById(id);
        if (v == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy biến thể");
            return "redirect:/admin/san-pham";
        }
        int productId = v.getProductId();
        variantService.delete(id);
        ra.addFlashAttribute("successMsg", "Đã xóa biến thể");
        return "redirect:/admin/san-pham/chi-tiet/" + productId;
    }

    @GetMapping("/bulk-edit/{productId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_UPDATE)")
    public String bulkEditForm(@PathVariable Integer productId, Model model, RedirectAttributes ra) {
        var p = productRepository.findById(productId).orElse(null);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        model.addAttribute("title", "san-pham");
        model.addAttribute("productId", productId);
        model.addAttribute("productName", p.getTenSanPham());
        model.addAttribute("variants", variantService.findByProductId(productId));
        return "view/admin/product/variant-bulk-edit";
    }

    @PostMapping("/api/bulk-save")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_UPDATE)")
    public ResponseEntity<Map<String, Object>> bulkSave(@RequestBody List<Map<String, Object>> variants) {
        Integer adminId = securityUtil.getCurrentUserId();
        variantService.bulkUpdate(variants, adminId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
