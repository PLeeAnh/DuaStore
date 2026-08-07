package com.duastore.controller.admin;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.repository.ProductRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.FlashSaleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/flash-sale")
public class AdminFlashSaleController {

    private final FlashSaleService flashSaleService;
    private final ProductRepository productRepository;
    private final NotificationHelper notificationHelper;

    public AdminFlashSaleController(FlashSaleService flashSaleService,
            ProductRepository productRepository,
            NotificationHelper notificationHelper) {
        this.flashSaleService = flashSaleService;
        this.productRepository = productRepository;
        this.notificationHelper = notificationHelper;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_READ)")
    public String list(Model model) {
        List<FlashSale> list = flashSaleService.getAll();
        model.addAttribute("flashSales", list);
        model.addAttribute("title", "flash-sale");
        model.addAttribute("promotionTab", "flash-sale");
        return "view/admin/flashsale/list";
    }

    @GetMapping("/create")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("flashSale", new FlashSaleFormDTO());
        model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
        model.addAttribute("productName", "");
        return "view/admin/flashsale/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            FlashSale fs = flashSaleService.getById(id);
            FlashSaleFormDTO dto = new FlashSaleFormDTO();
            dto.setId(fs.getId());
            dto.setProductId(fs.getProductId());
            dto.setGiaTriGiam(fs.getGiaTriGiam());
            dto.setNgayBatDau(fs.getNgayBatDau());
            dto.setNgayKetThuc(fs.getNgayKetThuc());
            dto.setIsActive(fs.getIsActive());
            model.addAttribute("flashSale", dto);
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("productName", productName(fs.getProductId()));
            return "view/admin/flashsale/form";
        } catch (Exception e) {
            return "redirect:/admin/flash-sale";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_CREATE) or "
            + "@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String save(@Valid @ModelAttribute("flashSale") FlashSaleFormDTO dto,
            BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("productName", productName(dto.getProductId()));
            return "view/admin/flashsale/form";
        }
        try {
            boolean isNew = dto.getId() == null;
            boolean wasActive = !isNew && Boolean.TRUE.equals(flashSaleService.getById(dto.getId()).getIsActive());
            FlashSale saved = flashSaleService.save(dto);
            boolean isActive = Boolean.TRUE.equals(saved.getIsActive());
            notifyStaffFlashSale(saved, isNew ? "tao" : "cap nhat");
            if ((isNew && isActive) || (!wasActive && isActive)) {
                notifyCustomersFlashSale(saved);
            }
            ra.addFlashAttribute("successMsg", "Lưu Flash Sale thành công");
        } catch (Exception e) {
            model.addAttribute("flashSale", dto);
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("productName", productName(dto.getProductId()));
            model.addAttribute("errorMsg", e.getMessage());
            return "view/admin/flashsale/form";
        }
        return "redirect:/admin/flash-sale";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            flashSaleService.delete(id);
            ra.addFlashAttribute("successMsg", "Xóa Flash Sale thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale";
    }

    @PostMapping("/toggle/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String toggle(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            FlashSale oldFlashSale = flashSaleService.getById(id);
            boolean wasActive = Boolean.TRUE.equals(oldFlashSale.getIsActive());
            flashSaleService.toggleActive(id);
            FlashSale updated = flashSaleService.getById(id);
            if (!wasActive && Boolean.TRUE.equals(updated.getIsActive())) {
                notifyCustomersFlashSale(updated);
                notifyStaffFlashSale(updated, "kich hoat");
            }
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale";
    }
    private void notifyCustomersFlashSale(FlashSale flashSale) {
        String productName = productName(flashSale.getProductId());
        notificationHelper.notifyAll(
                "Flash sale " + productName + " dang dien ra! Giam " + flashSale.getGiaTriGiam() + "%",
                "PROMOTION", flashSale.getId(),
                "/khuyen-mai",
                "Xem ngay"
        );
    }

    private void notifyStaffFlashSale(FlashSale flashSale, String action) {
        String productName = productName(flashSale.getProductId());
        notificationHelper.notifyStaff(
                "Flash sale " + productName + " da duoc " + action,
                "PROMOTION", flashSale.getId(),
                "/admin/flash-sale",
                "Xem flash sale"
        );
    }

    private String productName(Integer productId) {
        return productRepository.findById(productId)
                .map(Product::getTenSanPham)
                .orElse("san pham #" + productId);
    }
}
