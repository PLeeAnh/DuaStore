package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.ProductVariantFormDTO;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.WishlistRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminVariantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/bien-the")
public class AdminVariantController {

    private final AdminVariantService variantService;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final NotificationHelper notificationHelper;

    private final SecurityUtil securityUtil;
  
    public AdminVariantController(AdminVariantService variantService,
            ProductRepository productRepository,
            WishlistRepository wishlistRepository,
            NotificationHelper notificationHelper,
            SecurityUtil securityUtil) {
        this.variantService = variantService;
        this.productRepository = productRepository;
        this.wishlistRepository = wishlistRepository;
        this.notificationHelper = notificationHelper;
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
        ProductVariant oldVariant = variantService.findById(id);
        Integer oldStock = oldVariant != null ? oldVariant.getSoLuongTon() : 0;
        BigDecimal oldPrice = oldVariant != null ? effectivePrice(oldVariant) : null;
        var saved = variantService.save(dto);
        notifyVariantChanges(saved, oldStock, oldPrice);
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

    private void notifyVariantChanges(ProductVariant variant, Integer oldStock, BigDecimal oldPrice) {
        if (variant == null) {
            return;
        }
        String productName = productRepository.findById(variant.getProductId())
                .map(p -> p.getTenSanPham())
                .orElse("Sản phẩm #" + variant.getProductId());
        Integer newStock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
        if (oldStock != null && oldStock > 0 && newStock == 0) {
            notificationHelper.notifyStaff(
                    "Sản phẩm " + productName + " vừa hết hàng!",
                    "PRODUCT", variant.getProductId(),
                    "/admin/san-pham/sua/" + variant.getProductId(),
                    "Xem sản phẩm"
            );
        }

        boolean backInStock = oldStock == null || (oldStock <= 0 && newStock > 0);
        Optional<BigDecimal> droppedPrice = Optional.empty();
        BigDecimal newPrice = effectivePrice(variant);
        if (oldPrice != null && newPrice.compareTo(oldPrice) < 0) {
            droppedPrice = Optional.of(newPrice);
        }
        if (!backInStock && droppedPrice.isEmpty()) {
            return;
        }

        List<Integer> userIds = wishlistRepository.findUserIdsByProductId(variant.getProductId());
        for (Integer userId : userIds) {
            if (backInStock) {
                notificationHelper.notifyAll(
                        "Sản phẩm " + productName + " đã có hàng trở lại",
                        "PRODUCT", variant.getProductId(),
                        "/san-pham/" + variant.getProductId(),
                        "Xem ngay",
                        userId
                );
            }
            droppedPrice.ifPresent(price -> notificationHelper.notifyAll(
                    "Sản phẩm " + productName + " đã giảm giá còn " + formatCurrency(price),
                    "PRODUCT", variant.getProductId(),
                    "/san-pham/" + variant.getProductId(),
                    "Xem ngay",
                    userId
            ));
        }
    }

    private BigDecimal effectivePrice(ProductVariant variant) {
        return variant.getGiaKhuyenMai() != null ? variant.getGiaKhuyenMai() : variant.getGiaGoc();
    }

    private String formatCurrency(BigDecimal price) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(price);
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
