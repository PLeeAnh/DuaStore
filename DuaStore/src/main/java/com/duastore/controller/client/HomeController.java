package com.duastore.controller.client;

import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.CategoryService;
import com.duastore.service.client.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    public HomeController(ProductService productService,
                          CategoryService categoryService,
                          FlashSaleRepository flashSaleRepository,
                          ProductVariantRepository variantRepository,
                          ProductRepository productRepository) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.flashSaleRepository = flashSaleRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");

        List<Product> featured = productService.getFeatured();
        model.addAttribute("featuredProducts", featured);
        model.addAttribute("featuredCategories", categoryService.getFeaturedCategories());

        Map<Integer, Long> productCountMap = productRepository.countProductsByDanhMuc()
            .stream()
            .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1]));
        model.addAttribute("productCountMap", productCountMap);

        Map<Integer, FlashSale> flashSaleMap = new HashMap<>();
        Map<Integer, List<ProductVariant>> variantsMap = new HashMap<>();
        if (!featured.isEmpty()) {
            List<Integer> ids = featured.stream().map(Product::getId).collect(Collectors.toList());
            List<FlashSale> activeFlashSales = flashSaleRepository.findActiveNow(LocalDateTime.now());
            for (FlashSale fs : activeFlashSales) {
                flashSaleMap.put(fs.getProductId(), fs);
            }
            List<ProductVariant> allVariants = variantRepository.findByProductIdInAndIsActiveTrue(ids);
            variantsMap = allVariants.stream()
                .collect(Collectors.groupingBy(ProductVariant::getProductId));
        }
        model.addAttribute("flashSaleMap", flashSaleMap);
        model.addAttribute("variantsMap", variantsMap);

        // Group variants by cap type for card display
        Map<Integer, Map<String, List<ProductVariant>>> groupedVariantsMap = new HashMap<>();
        for (Map.Entry<Integer, List<ProductVariant>> entry : variantsMap.entrySet()) {
            Map<String, List<ProductVariant>> grouped = new LinkedHashMap<>();
            for (ProductVariant v : entry.getValue()) {
                String kieuNap = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) kieuNap = parts[1].trim();
                } else if (v.getDungTich() != null) {
                    kieuNap = "Dung tích";
                }
                grouped.computeIfAbsent(kieuNap, k -> new ArrayList<>()).add(v);
            }
            groupedVariantsMap.put(entry.getKey(), grouped);
        }
        model.addAttribute("groupedVariantsMap", groupedVariantsMap);

        return "view/client/index";
    }

    @GetMapping("/wishlist")
    public RedirectView wishlistRedirect() {
        return new RedirectView("/");
    }
}