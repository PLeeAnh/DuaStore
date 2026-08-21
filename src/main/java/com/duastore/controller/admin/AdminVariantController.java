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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin/bien-the")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới biến thể sản phẩm.
 */
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
        notifyVariantAfterCreate(saved);
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
        if (oldVariant != null && dto.getGiaGoc() != null && oldVariant.getGiaGoc() != null) {
            BigDecimal diff = dto.getGiaGoc().subtract(oldVariant.getGiaGoc()).abs();
            BigDecimal pct = diff.multiply(new BigDecimal("100")).divide(oldVariant.getGiaGoc(), 2, java.math.RoundingMode.HALF_UP);
            if (pct.compareTo(new BigDecimal("30")) > 0) {
                ra.addFlashAttribute("warningMsg", "Giá thay đổi " + pct.stripTrailingZeros().toPlainString()
                        + "% so với giá cũ (" + formatCurrency(oldVariant.getGiaGoc()) + "). Bạn có chắc muốn thay đổi?");
            }
        }
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

    private void notifyVariantAfterCreate(ProductVariant saved) {
        if (saved == null || saved.getSoLuongTon() == null || saved.getSoLuongTon() <= 0) return;
        List<ProductVariant> allVariants = variantService.findByProductId(saved.getProductId());
        boolean wasAllZero = allVariants.stream()
                .filter(v -> !v.getId().equals(saved.getId()))
                .allMatch(v -> v.getSoLuongTon() == null || v.getSoLuongTon() <= 0);
        if (!wasAllZero) return;
        String productName = productRepository.findById(saved.getProductId())
                .map(p -> p.getTenSanPham())
                .orElse("Sản phẩm #" + saved.getProductId());
        List<Integer> userIds = wishlistRepository.findUserIdsByProductId(saved.getProductId());
        for (Integer userId : userIds) {
            notificationHelper.notifyAll(
                    "Sản phẩm " + productName + " đã có hàng trở lại",
                    "PRODUCT", saved.getProductId(),
                    "/san-pham/" + saved.getProductId(),
                    "Xem ngay",
                    userId
            );
        }
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
    public String bulkEditForm(@PathVariable Integer productId,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model, RedirectAttributes ra) {
        var p = productRepository.findById(productId).orElse(null);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        model.addAttribute("title", "san-pham");
        model.addAttribute("productId", productId);
        model.addAttribute("productName", p.getTenSanPham());

        var paged = variantService.findByProductIdPaged(productId, page, size);
        model.addAttribute("variants", paged.getContent());
        model.addAttribute("totalElements", paged.getTotalElements());
        model.addAttribute("page", paged.getNumber());
        model.addAttribute("pageSize", paged.getSize());

        return "view/admin/product/variant-bulk-edit";
    }

    @PostMapping("/api/bulk-save")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).VARIANT_UPDATE)")
    public ResponseEntity<Map<String, Object>> bulkSave(@RequestBody List<Map<String, Object>> variants) {
        Map<Integer, Integer> oldStocks = new HashMap<>();
        Map<Integer, BigDecimal> oldPrices = new HashMap<>();

        for (Map<String, Object> entry : variants) {
            Integer id = toInteger(entry.get("id"));
            if (id == null) {
                continue;
            }
            ProductVariant oldVariant = variantService.findById(id);
            if (oldVariant != null) {
                oldStocks.put(id, oldVariant.getSoLuongTon());
                oldPrices.put(id, effectivePrice(oldVariant));
            }
        }

        Integer adminId = securityUtil.getCurrentUserId();
        variantService.bulkUpdate(variants, adminId);
        notifyBulkVariantChanges(oldStocks, oldPrices);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void notifyBulkVariantChanges(Map<Integer, Integer> oldStocks, Map<Integer, BigDecimal> oldPrices) {
        Set<Integer> restockedProductIds = new HashSet<>();
        Map<Integer, BigDecimal> droppedPrices = new HashMap<>();

        for (Integer variantId : oldStocks.keySet()) {
            ProductVariant variant = variantService.findById(variantId);
            if (variant == null) {
                continue;
            }

            Integer oldStock = oldStocks.getOrDefault(variantId, 0);
            Integer newStock = variant.getSoLuongTon() != null ? variant.getSoLuongTon() : 0;
            if (oldStock != null && oldStock > 0 && newStock == 0) {
                String productName = productName(variant.getProductId());
                notificationHelper.notifyStaff(
                        "Sản phẩm " + productName + " vừa hết hàng!",
                        "PRODUCT", variant.getProductId(),
                        "/admin/san-pham/sua/" + variant.getProductId(),
                        "Xem sản phẩm"
                );
            }
            if (oldStock != null && oldStock <= 0 && newStock > 0) {
                restockedProductIds.add(variant.getProductId());
            }

            BigDecimal oldPrice = oldPrices.get(variantId);
            BigDecimal newPrice = effectivePrice(variant);
            if (oldPrice != null && newPrice != null && newPrice.compareTo(oldPrice) < 0) {
                droppedPrices.merge(variant.getProductId(), newPrice, BigDecimal::min);
            }
        }

        for (Integer productId : restockedProductIds) {
            notifyWishlistUsersBackInStock(productId);
        }
        for (Map.Entry<Integer, BigDecimal> entry : droppedPrices.entrySet()) {
            notifyWishlistUsersPriceDropped(entry.getKey(), entry.getValue());
        }
    }

    private void notifyWishlistUsersBackInStock(Integer productId) {
        String productName = productName(productId);
        List<Integer> userIds = wishlistRepository.findUserIdsByProductId(productId);
        for (Integer userId : userIds) {
            notificationHelper.notifyAll(
                    "Sản phẩm " + productName + " đã có hàng trở lại",
                    "PRODUCT", productId,
                    "/san-pham/" + productId,
                    "Xem ngay",
                    userId
            );
        }
    }

    private void notifyWishlistUsersPriceDropped(Integer productId, BigDecimal price) {
        String productName = productName(productId);
        List<Integer> userIds = wishlistRepository.findUserIdsByProductId(productId);
        for (Integer userId : userIds) {
            notificationHelper.notifyAll(
                    "Sản phẩm " + productName + " đã giảm giá còn " + formatCurrency(price),
                    "PRODUCT", productId,
                    "/san-pham/" + productId,
                    "Xem ngay",
                    userId
            );
        }
    }

    private String productName(Integer productId) {
        return productRepository.findById(productId)
                .map(p -> p.getTenSanPham())
                .orElse("Sản phẩm #" + productId);
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
