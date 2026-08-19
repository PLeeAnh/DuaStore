package com.duastore.controller.admin;

import com.duastore.dto.FlashSaleFormDTO;
import com.duastore.dto.FlashSaleItemFormDTO;
import com.duastore.model.FlashSale;
import com.duastore.model.FlashSaleItem;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.PricingService;
import com.duastore.service.admin.FlashSaleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/flash-sale")
public class AdminFlashSaleController {

    private final FlashSaleService flashSaleService;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final PricingService pricingService;
    private final NotificationHelper notificationHelper;

    public AdminFlashSaleController(FlashSaleService flashSaleService,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            PricingService pricingService,
            NotificationHelper notificationHelper) {
        this.flashSaleService = flashSaleService;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.pricingService = pricingService;
        this.notificationHelper = notificationHelper;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_READ)")
    public String list(Model model) {
        List<FlashSale> list = flashSaleService.getAll();
        Map<Integer, String> statusMap = new LinkedHashMap<>();
        Map<Integer, Long> revenueMap = new LinkedHashMap<>();
        Map<Integer, Integer> soldMap = new LinkedHashMap<>();
        Map<Integer, Integer> itemCountMap = new LinkedHashMap<>();
        for (FlashSale fs : list) {
            statusMap.put(fs.getId(), pricingService.getEventStatus(fs));
            revenueMap.put(fs.getId(), pricingService.sumRevenue(fs));
            soldMap.put(fs.getId(), pricingService.sumSold(fs));
            itemCountMap.put(fs.getId(), fs.getItems() != null ? fs.getItems().size() : 0);
        }
        model.addAttribute("flashSales", list);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("revenueMap", revenueMap);
        model.addAttribute("soldMap", soldMap);
        model.addAttribute("itemCountMap", itemCountMap);
        model.addAttribute("title", "flash-sale");
        model.addAttribute("promotionTab", "flash-sale");
        return "view/admin/flashsale/list";
    }

    @GetMapping("/create")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("flashSale", new FlashSaleFormDTO());
        model.addAttribute("isNew", true);
        model.addAttribute("items", new ArrayList<FlashSaleItem>());
        model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
        model.addAttribute("title", "flash-sale");
        return "view/admin/flashsale/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            FlashSale fs = flashSaleService.getById(id);
            FlashSaleFormDTO dto = new FlashSaleFormDTO();
            dto.setId(fs.getId());
            dto.setTenChuongTrinh(fs.getTenChuongTrinh());
            dto.setMoTa(fs.getMoTa());
            dto.setNgayBatDau(fs.getNgayBatDau());
            dto.setNgayKetThuc(fs.getNgayKetThuc());
            dto.setIsActive(fs.getIsActive());
            dto.setPriority(fs.getPriority());
            model.addAttribute("flashSale", dto);
            model.addAttribute("isNew", false);
            model.addAttribute("flashSaleId", id);
            model.addAttribute("items", fs.getItems());
            model.addAttribute("itemProductMap", buildItemProductMap(fs.getItems()));
            model.addAttribute("itemVariantMap", buildItemVariantMap(fs.getItems()));
            model.addAttribute("flashSaleItem", new FlashSaleItemFormDTO());
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("status", pricingService.getEventStatus(fs));
            model.addAttribute("revenue", pricingService.sumRevenue(fs));
            model.addAttribute("sold", pricingService.sumSold(fs));
            model.addAttribute("title", "flash-sale");
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
        boolean isNew = dto.getId() == null;
        if (result.hasErrors()) {
            String firstError = result.getAllErrors().isEmpty() ? null : result.getAllErrors().get(0).getDefaultMessage();
            if (firstError != null) {
                model.addAttribute("errorMsg", firstError);
            }
            populateFormModel(model, dto, isNew);
            return "view/admin/flashsale/form";
        }
        try {
            FlashSale saved = flashSaleService.save(dto);
            ra.addFlashAttribute("successMsg", "Lưu Flash Sale thành công");
            if (isNew) {
                return "redirect:/admin/flash-sale/edit/" + saved.getId();
            }
        } catch (Exception e) {
            model.addAttribute("errorMsg", e.getMessage());
            populateFormModel(model, dto, isNew);
            return "view/admin/flashsale/form";
        }
        return "redirect:/admin/flash-sale";
    }

    private void populateFormModel(Model model, FlashSaleFormDTO dto, boolean isNew) {
        model.addAttribute("flashSale", dto);
        model.addAttribute("isNew", isNew);
        model.addAttribute("title", "flash-sale");
        model.addAttribute("promotionTab", "flash-sale");
        model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
        model.addAttribute("flashSaleItem", new FlashSaleItemFormDTO());
        if (isNew || dto.getId() == null) {
            model.addAttribute("items", new ArrayList<FlashSaleItem>());
            return;
        }
        try {
            FlashSale fs = flashSaleService.getById(dto.getId());
            model.addAttribute("flashSaleId", fs.getId());
            model.addAttribute("items", fs.getItems());
            model.addAttribute("itemProductMap", buildItemProductMap(fs.getItems()));
            model.addAttribute("itemVariantMap", buildItemVariantMap(fs.getItems()));
            model.addAttribute("status", pricingService.getEventStatus(fs));
            model.addAttribute("revenue", pricingService.sumRevenue(fs));
            model.addAttribute("sold", pricingService.sumSold(fs));
        } catch (Exception ex) {
            model.addAttribute("flashSaleId", dto.getId());
            model.addAttribute("items", new ArrayList<FlashSaleItem>());
        }
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String addItem(@PathVariable Integer id,
            @Valid @ModelAttribute("flashSaleItem") FlashSaleItemFormDTO itemDto,
            BindingResult result, RedirectAttributes ra) {
        itemDto.setFlashSaleId(id);
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMsg",
                    result.getAllErrors().isEmpty() ? "Dữ liệu không hợp lệ" : result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/flash-sale/edit/" + id;
        }
        try {
            flashSaleService.addItem(itemDto);
            ra.addFlashAttribute("successMsg", "Thêm sản phẩm flash sale thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale/edit/" + id;
    }

    @PostMapping("/items/{itemId}/delete")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_DELETE)")
    public String deleteItem(@PathVariable Integer itemId, @RequestParam Integer flashSaleId, RedirectAttributes ra) {
        try {
            flashSaleService.deleteItem(itemId);
            ra.addFlashAttribute("successMsg", "Xóa sản phẩm flash sale thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale/edit/" + flashSaleId;
    }

    @PostMapping("/items/{itemId}/toggle")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_UPDATE)")
    public String toggleItem(@PathVariable Integer itemId, @RequestParam Integer flashSaleId, RedirectAttributes ra) {
        try {
            flashSaleService.toggleItem(itemId);
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái sản phẩm thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale/edit/" + flashSaleId;
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
                notifyStaffFlashSale(updated, "kich hoat");
            }
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/flash-sale";
    }

    @GetMapping("/variants")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).FLASH_SALE_READ)")
    public List<Map<String, Object>> variantsByProduct(@RequestParam Integer productId) {
        List<Map<String, Object>> result = new ArrayList<>();
        variantRepository.findByProductIdAndIsActiveTrue(productId)
                .forEach(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", v.getId());
                    m.put("tenBienThe", v.getTenBienThe());
                    m.put("giaGoc", v.getGiaGoc());
                    m.put("stock", v.getSoLuongTon());
                    result.add(m);
                });
        return result;
    }

    private Map<Integer, Product> buildItemProductMap(List<FlashSaleItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        Map<Integer, ProductVariant> variants = buildItemVariantMap(items);
        List<Integer> productIds = variants.values().stream()
                .map(ProductVariant::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Product> productsById = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
        Map<Integer, Product> products = new LinkedHashMap<>();
        for (FlashSaleItem item : items) {
            ProductVariant v = variants.get(item.getId());
            if (v != null) {
                Product p = productsById.get(v.getProductId());
                if (p != null) {
                    products.put(item.getId(), p);
                }
            }
        }
        return products;
    }

    private Map<Integer, ProductVariant> buildItemVariantMap(List<FlashSaleItem> items) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        List<Integer> variantIds = items.stream()
                .map(FlashSaleItem::getVariantId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (variantIds.isEmpty()) {
            return Map.of();
        }
        Map<Integer, ProductVariant> variantsById = variantRepository.findAllById(variantIds)
                .stream().collect(Collectors.toMap(ProductVariant::getId, v -> v, (a, b) -> a));
        Map<Integer, ProductVariant> byItemId = new LinkedHashMap<>();
        for (FlashSaleItem item : items) {
            ProductVariant v = variantsById.get(item.getVariantId());
            if (v != null) {
                byItemId.put(item.getId(), v);
            }
        }
        return byItemId;
    }

    private void notifyStaffFlashSale(FlashSale flashSale, String action) {
        notificationHelper.notifyStaff(
                "Flash sale \"" + flashSale.getTenChuongTrinh() + "\" da duoc " + action,
                "PROMOTION", flashSale.getId(),
                "/admin/flash-sale",
                "Xem flash sale"
        );
    }
}