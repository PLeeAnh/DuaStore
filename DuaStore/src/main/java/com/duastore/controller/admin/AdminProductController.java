package com.duastore.controller.admin;

import com.duastore.dto.ProductFormDTO;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.admin.AdminProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/san-pham")
public class AdminProductController {

    private final AdminProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    public AdminProductController(AdminProductService productService,
                                   CategoryRepository categoryRepository,
                                   ProductImageRepository productImageRepository,
                                   ProductVariantRepository productVariantRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String trangThai,
                       Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");

        if (keyword != null && keyword.isBlank()) keyword = null;
        if (trangThai != null && trangThai.isBlank()) trangThai = null;

        boolean hasFilter = (keyword != null)
                         || danhMuc != null
                         || (trangThai != null);

        Page<Product> productPage;
        if (hasFilter) {
            productPage = productService.searchPaged(keyword, danhMuc, trangThai, page, size);
        } else {
            productPage = productService.findAllPaged(page, size);
        }
        model.addAttribute("products", productPage.getContent());

        model.addAttribute("keyword", keyword);
        model.addAttribute("danhMuc", danhMuc);
        model.addAttribute("trangThai", trangThai);
        List<Category> cats = categoryRepository.findByIsActiveTrue();
        model.addAttribute("categories", cats);
        model.addAttribute("categoryMap", cats.stream().collect(Collectors.toMap(Category::getId, Category::getTenDanhMuc)));

        Map<Integer, Integer> totalStock = new HashMap<>();
        List<Product> products = productPage.getContent();
        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> allVariants = productVariantRepository.findByProductIdInAndIsActiveTrue(ids);
            for (ProductVariant v : allVariants) {
                totalStock.merge(v.getProductId(), v.getSoLuongTon(), Integer::sum);
            }
        }
        model.addAttribute("totalStock", totalStock);

        Map<String, Object> filterParams = new HashMap<>();
        if (keyword != null) filterParams.put("keyword", keyword);
        if (danhMuc != null) filterParams.put("danhMuc", danhMuc);
        if (trangThai != null) filterParams.put("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "sản phẩm");
        model.addAttribute("url", "/admin/san-pham");
        model.addAttribute("filterParams", filterParams);

        return "view/admin/product/product-list";
    }

    @GetMapping("/bien-the")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String variantPage(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              @RequestParam(required = false) String keyword,
                              Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "bien-the");

        boolean hasFilter = keyword != null && !keyword.isBlank();
        Page<ProductVariant> variantPage;
        if (hasFilter) {
            variantPage = productVariantRepository.searchAllPaged(keyword, PageRequest.of(page, size));
        } else {
            variantPage = productVariantRepository.findByIsActiveTrueOrderByIdAsc(PageRequest.of(page, size));
        }

        Map<Integer, String> productNames = new HashMap<>();
        for (ProductVariant v : variantPage.getContent()) {
            if (!productNames.containsKey(v.getProductId())) {
                Product p = productService.findById(v.getProductId());
                productNames.put(v.getProductId(), p != null ? p.getTenSanPham() : "Đã xóa");
            }
        }

        Map<String, Object> filterParams = new HashMap<>();
        if (hasFilter) filterParams.put("keyword", keyword);

        model.addAttribute("variants", variantPage.getContent());
        model.addAttribute("productNames", productNames);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", variantPage.getTotalPages());
        model.addAttribute("totalItems", variantPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "biến thể");
        model.addAttribute("url", "/admin/san-pham/bien-the");
        model.addAttribute("filterParams", filterParams);
        return "view/admin/product/variant-page";
    }

    @GetMapping("/thong-so")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String specPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-so");

        Page<Product> productPage = productService.findAllPaged(page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "sản phẩm");
        model.addAttribute("url", "/admin/san-pham/thong-so");
        model.addAttribute("filterParams", new HashMap<>());
        return "view/admin/product/spec-page";
    }

    @GetMapping("/hinh-anh")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_READ)")
    public String imagePage(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "hinh-anh");

        Page<Product> productPage = productService.findAllPaged(page, size);
        Map<Integer, Integer> imageCounts = new HashMap<>();
        for (Product p : productPage.getContent()) {
            imageCounts.put(p.getId(),
                productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(p.getId()).size());
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("imageCounts", imageCounts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "sản phẩm");
        model.addAttribute("url", "/admin/san-pham/hinh-anh");
        model.addAttribute("filterParams", new HashMap<>());
        return "view/admin/product/image-page";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "san-pham");
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", new ProductFormDTO());
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        return "view/admin/product/product-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_CREATE)")
    public String create(@Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            return "view/admin/product/product-form";
        }
        productService.save(dto);
        ra.addFlashAttribute("successMsg", "Thêm sản phẩm thành công");
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        Product p = productService.findById(id);
        if (p == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }

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
        model.addAttribute("productTab", "thong-tin");
        model.addAttribute("product", dto);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        model.addAttribute("galleryImages", productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
        return "view/admin/product/product-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_UPDATE)")
    public String edit(@PathVariable Integer id, @Valid ProductFormDTO dto, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("title", "san-pham");
            model.addAttribute("productTab", "thong-tin");
            model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
            model.addAttribute("galleryImages", productImageRepository.findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id));
            return "view/admin/product/product-form";
        }
        dto.setId(id);
        Product saved = productService.save(dto);
        if (saved == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm");
            return "redirect:/admin/san-pham";
        }
        ra.addFlashAttribute("successMsg", "Cập nhật sản phẩm thành công");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("successMsg", "Đã xóa sản phẩm");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/xoa-anh/{imageId}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).PRODUCT_DELETE)")
    public String deleteImage(@PathVariable Integer imageId, RedirectAttributes ra) {
        ProductImage img = productImageRepository.findById(imageId).orElse(null);
        if (img == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy ảnh");
            return "redirect:/admin/san-pham";
        }
        Integer productId = img.getProductId();
        img.setActive(false);
        productImageRepository.save(img);
        ra.addFlashAttribute("successMsg", "Đã xóa ảnh");
        return "redirect:/admin/san-pham/sua/" + productId;
    }

}
