package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.CartItemDTO;
import com.duastore.model.Product;
import com.duastore.model.ProductVariant;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.CartService;
import com.duastore.service.client.SavedCartService;
import com.duastore.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
/**
 * Controller xử lý các request HTTP liên quan tới giỏ hàng.
 */
public class CartController {

    private final CartService cartService;
    private final SavedCartService savedCartService;
    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final SecurityUtil securityUtil;
    private final RecommendationService recommendationService;

    public CartController(CartService cartService, SavedCartService savedCartService,
            ProductVariantRepository variantRepository,
            ProductRepository productRepository, SecurityUtil securityUtil,
            RecommendationService recommendationService) {
        this.cartService = cartService;
        this.savedCartService = savedCartService;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.securityUtil = securityUtil;
        this.recommendationService = recommendationService;
    }

    @SuppressWarnings("unchecked")
    private List<CartItemDTO> buildGuestCartItems(HttpSession session) {
        Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");
        if (guestCart == null || guestCart.isEmpty()) {
            return List.of();
        }
        List<CartItemDTO> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : guestCart.entrySet()) {
            ProductVariant v = variantRepository.findById(e.getKey()).orElse(null);
            if (v == null || !v.isActive()) {
                continue;
            }
            Product p = v.getProduct();
            if (p == null) {
                continue;
            }
            CartItemDTO dto = new CartItemDTO();
            dto.setVariantId(v.getId());
            dto.setProductId(p.getId());
            dto.setTenSanPham(p.getTenSanPham());
            dto.setTenBienThe(v.getTenBienThe());
            dto.setHinhAnh(v.getHinhAnh() != null ? v.getHinhAnh() : p.getHinhAnhChinh());
            BigDecimal price = v.getGiaKhuyenMai() != null ? v.getGiaKhuyenMai() : (v.getGiaGoc() != null ? v.getGiaGoc() : BigDecimal.ZERO);
            dto.setGiaBan(price);
            dto.setSoLuong(e.getValue());
            dto.setThanhTien(price.multiply(BigDecimal.valueOf(e.getValue())));
            items.add(dto);
        }
        return items;
    }

    @GetMapping("/gio-hang")
    public String cart(HttpSession session, Model model) {
        Integer userId = currentUserId();

        if (userId == null) {
            List<CartItemDTO> guestItems = buildGuestCartItems(session);
            model.addAttribute("title", "gio-hang");
            model.addAttribute("cartItems", guestItems);
            model.addAttribute("totalPrice", cartService.total(guestItems));
            model.addAttribute("cartCount", guestItems.size());
            model.addAttribute("savedItems", List.of());
            model.addAttribute("savedCount", 0);
            model.addAttribute("suggestions", List.of());
            model.addAttribute("isGuest", true);
            model.addAttribute("stockWarnings", List.of());
            model.addAttribute("hasOutOfStock", false);
            model.addAttribute("hasStockWarnings", false);
            model.addAttribute("hasPriceChanges", false);
            return "view/client/cart/cart";
        }

        List<CartItemDTO> items = cartService.getItems(userId);
        model.addAttribute("title", "gio-hang");
        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", cartService.total(items));
        model.addAttribute("cartCount", cartService.count(userId));

        model.addAttribute("savedItems", savedCartService.getSavedItems(userId));
        model.addAttribute("savedCount", savedCartService.count(userId));

        List<Product> suggestions = recommendationService.getPersonalizedSuggestions(userId, 8);
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

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getGuestCart(HttpSession session) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("guestCart");
        if (cart == null) {
            cart = new java.util.LinkedHashMap<>();
            session.setAttribute("guestCart", cart);
        }
        return cart;
    }

    @PostMapping("/api/cart/save-for-later")
    @ResponseBody
    public Map<String, Object> saveForLater(@RequestBody Map<String, Integer> body, HttpSession session) {
        Integer variantId = body.get("variantId");
        if (variantId == null) {
            return failResponse("Thiếu thông tin");
        }
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
        if (savedId == null) {
            return failResponse("Thiếu thông tin");
        }
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
        if (savedId == null) {
            return failResponse("Thiếu thông tin");
        }
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

    @PostMapping("/gio-hang/mua-lai/{orderId}")
    public String reorder(@PathVariable Integer orderId, RedirectAttributes ra) {
        Integer userId = currentUserId();
        if (userId == null) {
            return "redirect:/dang-nhap";
        }
        try {
            cartService.reorder(userId, orderId);
            ra.addFlashAttribute("successMsg", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/gio-hang";
    }

    private Integer currentUserId() {
        return securityUtil.getCurrentUserId();
    }
}
