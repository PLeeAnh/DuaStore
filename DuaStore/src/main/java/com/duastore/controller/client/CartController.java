package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.model.Product;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.CartService;
import com.duastore.service.client.SavedCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;
    private final SavedCartService savedCartService;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SecurityUtil securityUtil;

    public CartController(CartService cartService, SavedCartService savedCartService,
                          ProductVariantRepository variantRepository,
                          ProductRepository productRepository, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.savedCartService = savedCartService;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/gio-hang")
    public String cart(HttpSession session, Model model) {
        Integer userId = currentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        List<CartItemDTO> items = cartService.getItems(userId);
        model.addAttribute("title", "gio-hang");
        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", cartService.total(items));
        model.addAttribute("cartCount", cartService.count(userId));

        model.addAttribute("savedItems", savedCartService.getSavedItems(userId));
        model.addAttribute("savedCount", savedCartService.count(userId));

        List<Product> suggestions = cartService.getSuggestions(userId, 8);
        model.addAttribute("suggestions", suggestions);

        model.addAttribute("stockWarnings", cartService.getStockWarnings(userId));
        model.addAttribute("hasOutOfStock", cartService.hasOutOfStockItems(userId));
        model.addAttribute("hasStockWarnings", cartService.hasStockWarnings(userId));
        model.addAttribute("hasPriceChanges", cartService.hasPriceChanges(userId));

        return "view/client/cart/cart";
    }

    @PostMapping("/gio-hang/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam Integer soLuong,
                         HttpSession session) {
        Integer userId = currentUserId();
        if (userId != null) {
            cartService.updateQuantity(userId, id, soLuong);
        }
        return "redirect:/gio-hang";
    }

    @GetMapping("/gio-hang/xoa/{id}")
    public String remove(@PathVariable Integer id, HttpSession session) {
        Integer userId = currentUserId();
        if (userId != null) {
            cartService.remove(userId, id);
        }
        return "redirect:/gio-hang";
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public Map<String, Object> add(@RequestBody Map<String, Integer> body, HttpSession session) {
        Integer variantId = body.get("variantId");
        if (variantId == null && body.get("productId") != null) {
            variantId = variantRepository.findByProductIdAndIsDefaultTrue(body.get("productId"))
                    .or(() -> variantRepository.findByProductIdAndIsActiveTrue(body.get("productId")).stream().findFirst())
                    .map(v -> v.getId())
                    .orElse(null);
        }
        Integer quantity = body.get("soLuong") != null ? body.get("soLuong") : body.get("quantity");
        CartService.CartResult result = cartService.add(currentUserId(), variantId, quantity);
        return response(result);
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public Map<String, Object> count(HttpSession session) {
        Map<String, Object> data = new HashMap<>();
        data.put("cartCount", cartService.count(currentUserId()));
        return data;
    }

    @PostMapping("/api/cart/save-for-later")
    @ResponseBody
    public Map<String, Object> saveForLater(@RequestBody Map<String, Integer> body, HttpSession session) {
        Integer variantId = body.get("variantId");
        if (variantId == null) return failResponse("Thiếu thông tin");
        boolean ok = savedCartService.saveForLater(currentUserId(), variantId);
        Map<String, Object> data = new HashMap<>();
        data.put("success", ok);
        data.put("cartCount", cartService.count(currentUserId()));
        data.put("savedCount", savedCartService.count(currentUserId()));
        return data;
    }

    @PostMapping("/api/cart/move-to-cart")
    @ResponseBody
    public Map<String, Object> moveToCart(@RequestBody Map<String, Integer> body, HttpSession session) {
        Integer savedId = body.get("savedId");
        if (savedId == null) return failResponse("Thiếu thông tin");
        boolean ok = savedCartService.moveToCart(currentUserId(), savedId);
        Map<String, Object> data = new HashMap<>();
        data.put("success", ok);
        data.put("cartCount", cartService.count(currentUserId()));
        data.put("savedCount", savedCartService.count(currentUserId()));
        return data;
    }

    @PostMapping("/api/cart/remove-saved")
    @ResponseBody
    public Map<String, Object> removeSaved(@RequestBody Map<String, Integer> body, HttpSession session) {
        Integer savedId = body.get("savedId");
        if (savedId == null) return failResponse("Thiếu thông tin");
        savedCartService.removeSaved(currentUserId(), savedId);
        Map<String, Object> data = new HashMap<>();
        data.put("success", true);
        data.put("savedCount", savedCartService.count(currentUserId()));
        return data;
    }

    @GetMapping("/api/cart/suggestions")
    @ResponseBody
    public Map<String, Object> suggestions(HttpSession session) {
        Map<String, Object> data = new HashMap<>();
        List<Product> products = cartService.getSuggestions(currentUserId(), 8);
        List<Map<String, Object>> items = products.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("tenSanPham", p.getTenSanPham());
            item.put("hinhAnhChinh", p.getHinhAnhChinh());
            return item;
        }).toList();
        data.put("products", items);
        return data;
    }

    private Map<String, Object> response(CartService.CartResult result) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", result.success());
        data.put("message", result.message());
        data.put("cartCount", result.cartCount());
        return data;
    }

    private Map<String, Object> failResponse(String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", message);
        return data;
    }

    private Integer currentUserId() {
        return securityUtil.getCurrentUserId();
    }
}
