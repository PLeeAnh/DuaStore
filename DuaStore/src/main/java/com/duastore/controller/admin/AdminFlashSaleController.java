package com.duastore.controller.admin;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.repository.ProductRepository;
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

    public AdminFlashSaleController(FlashSaleService flashSaleService,
            ProductRepository productRepository) {
        this.flashSaleService = flashSaleService;
        this.productRepository = productRepository;
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
            return "view/admin/flashsale/form";
        }
        try {
            flashSaleService.save(dto);
            ra.addFlashAttribute("successMsg", "Lưu Flash Sale thành công");
        } catch (Exception e) {
            model.addAttribute("flashSale", dto);
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
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
            flashSaleService.toggleActive(id);
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale";
    }
}
