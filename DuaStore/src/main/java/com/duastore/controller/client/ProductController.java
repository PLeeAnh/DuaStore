package com.duastore.controller.client;

import com.duastore.dto.ReviewRequestDTO;
import com.duastore.dto.VariantApiDTO;
import com.duastore.model.Category;
import com.duastore.model.Product;
import com.duastore.model.ProductImage;
import com.duastore.model.ProductVariant;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductImageRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.ProductService;
import com.duastore.service.client.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    private final ProductService productService;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewService reviewService;
    private final JdbcTemplate jdbcTemplate;

    public ProductController(ProductService productService,
                             ProductVariantRepository variantRepository,
                             ProductImageRepository productImageRepository,
                             CategoryRepository categoryRepository,
                             ReviewService reviewService,
                             JdbcTemplate jdbcTemplate) {
        this.productService = productService;
        this.variantRepository = variantRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.reviewService = reviewService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/san-pham")
    public String list(@RequestParam(required = false) Integer danhMuc,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("title", "san-pham");

        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.search(keyword);
            model.addAttribute("keyword", keyword);
        } else if (danhMuc != null) {
            List<Integer> categoryIds = new ArrayList<>();
            categoryIds.add(danhMuc);
            categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(danhMuc)
                    .forEach(child -> categoryIds.add(child.getId()));
            products = productService.findByCategories(categoryIds);
            categoryRepository.findById(danhMuc).ifPresent(c -> model.addAttribute("selectedCategory", c));
        } else {
            products = productService.getDangBan();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc());
        model.addAttribute("selectedCategoryId", danhMuc);

        // Build price map: productId → hiển thị giá variant đầu tiên
        Map<Integer, String> priceMap = new HashMap<>();
        if (!products.isEmpty()) {
            List<Integer> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductVariant> allVariants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
            Map<Integer, List<ProductVariant>> byProduct = allVariants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));
            for (var entry : byProduct.entrySet()) {
                ProductVariant v = entry.getValue().get(0);
                BigDecimal price = v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : v.getGiaGoc();
                priceMap.put(entry.getKey(), NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price) + "₫");
            }
        }
        model.addAttribute("priceMap", priceMap);

        // CODE BỔ SUNG: Lấy danh sách ID sản phẩm đã yêu thích để bôi đỏ icon trái tim ở trang danh sách
        try {
            Integer userId = 2; // Gán cứng tài khoản Nguyễn Văn An (id=2) để test
            List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
            model.addAttribute("likedIds", likedIds);
        } catch (Exception e) {
            System.out.println("Lỗi đọc likedIds ở trang danh sách sản phẩm: " + e.getMessage());
        }

        return "view/client/product/product-list";
    }

    @GetMapping("/san-pham/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var product = productService.findById(id);
        if (product == null) return "redirect:/san-pham?errorMsg=Khong+tim+thay+san+pham";

        List<ProductVariant> variants = productService.getVariants(id);
        model.addAttribute("title", product.getTenSanPham());
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);

        // Build gallery images: ProductImages from DB + fallback to main + variant images
        List<ProductImage> dbImages = productImageRepository
            .findByProductIdAndIsActiveTrueOrderBySortOrderAscCreatedAtAsc(id);
        List<String> galleryImages = new ArrayList<>();
        if (!dbImages.isEmpty()) {
            for (ProductImage pi : dbImages) {
                if (pi.getImageUrl() != null) galleryImages.add(pi.getImageUrl());
            }
        } else {
            if (product.getHinhAnhChinh() != null) galleryImages.add(product.getHinhAnhChinh());
            for (ProductVariant v : variants) {
                if (v.getHinhAnh() != null && !galleryImages.contains(v.getHinhAnh())) {
                    galleryImages.add(v.getHinhAnh());
                }
            }
        }
        model.addAttribute("galleryImages", galleryImages);

        // Group variants by cap type (parsed from tenBienThe, e.g. "50ml - Nắp Gỗ")
        Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            String capType = "Khác";
            if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                if (parts.length >= 2) capType = parts[1].trim();
            }
            grouped.computeIfAbsent(capType, k -> new ArrayList<>()).add(v);
        }
        model.addAttribute("groupedVariants", grouped);

        // CODE BỔ SUNG: Lấy danh sách ID sản phẩm đã yêu thích để bôi đỏ icon trái tim ở trang chi tiết
        try {
            Integer userId = 2; // Gán cứng tài khoản Nguyễn Văn An (id=2) để test
            List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
            model.addAttribute("likedIds", likedIds);
        } catch (Exception e) {
            System.out.println("Lỗi đọc likedIds ở trang chi tiết sản phẩm: " + e.getMessage());
        }

        // Đánh giá sản phẩm
        model.addAttribute("reviews", reviewService.getApprovedReviews(id));
        try {
            Integer userId = 2;
            model.addAttribute("hasReviewed", reviewService.hasReviewed(userId, id));
        } catch (Exception e) {
            model.addAttribute("hasReviewed", false);
        }

        return "view/client/product/product-detail";
    }

    // ── Danh gia san pham ──

    @PostMapping("/san-pham/{id}/danh-gia")
    public String submitReview(@PathVariable Integer id,
                               @ModelAttribute ReviewRequestDTO request,
                               RedirectAttributes ra) {
        Integer userId = 2;
        request.setProductId(id);
        try {
            reviewService.createReview(userId, request);
            ra.addFlashAttribute("successMsg", "Cảm ơn bạn đã đánh giá! Đánh giá sẽ được hiển thị sau khi duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/san-pham/" + id;
    }

    // ── API for variant switching (AJAX) ──

    @GetMapping("/api/variants/{variantId}")
    @ResponseBody
    public VariantApiDTO getVariant(@PathVariable Integer variantId) {
        return productService.getVariantApi(variantId);
    }
}