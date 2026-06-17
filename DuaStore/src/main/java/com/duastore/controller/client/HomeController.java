package com.duastore.controller.client;

import com.duastore.model.FlashSale;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.FlashSaleRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final FlashSaleRepository flashSaleRepository;
    private final ProductVariantRepository variantRepository;

    public HomeController(ProductService productService,
                          FlashSaleRepository flashSaleRepository,
                          ProductVariantRepository variantRepository) {
        this.productService = productService;
        this.flashSaleRepository = flashSaleRepository;
        this.variantRepository = variantRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");

        List<Product> featured = productService.getFeatured();
        model.addAttribute("featuredProducts", featured);

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
                String capType = "Phân loại";
                if (v.getTenBienThe() != null && v.getTenBienThe().contains(" - ")) {
                    String[] parts = v.getTenBienThe().split("\\s*-\\s*");
                    if (parts.length >= 2) capType = parts[1].trim();
                } else if (v.getDungTich() != null) {
                    capType = "Dung tích";
                }
                grouped.computeIfAbsent(capType, k -> new ArrayList<>()).add(v);
            }
            groupedVariantsMap.put(entry.getKey(), grouped);
        }
        model.addAttribute("groupedVariantsMap", groupedVariantsMap);

        return "view/client/index";
    }
}