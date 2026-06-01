package com.duastore.controller.client;

import com.duastore.dto.CartItemDTO;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.service.client.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;
    private final ProductVariantRepository variantRepository;

    public CartController(CartService cartService, ProductVariantRepository variantRepository) {
        this.cartService = cartService;
        this.variantRepository = variantRepository;
    }

    @GetMapping("/gio-hang")
    public String cart(HttpSession session, Model model) {
        Integer userId = currentUserId(session);
        List<CartItemDTO> items = cartService.getItems(userId);
        model.addAttribute("title", "gio-hang");
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartService.total(items));
        return "view/client/cart/cart";
    }

    @PostMapping("/gio-hang/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam Integer soLuong,
                         HttpSession session) {
        cartService.updateQuantity(currentUserId(session), id, soLuong);
        return "redirect:/gio-hang";
    }

    @GetMapping("/gio-hang/xoa/{id}")
    public String remove(@PathVariable Integer id, HttpSession session) {
        cartService.remove(currentUserId(session), id);
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
        CartService.CartResult result = cartService.add(currentUserId(session), variantId, quantity);
        return response(result);
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public Map<String, Object> updateApi(@RequestBody Map<String, Integer> body, HttpSession session) {
        CartService.CartResult result = cartService.updateQuantity(
                currentUserId(session),
                body.get("itemId"),
                body.get("soLuong")
        );
        return response(result);
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public Map<String, Object> count(HttpSession session) {
        Map<String, Object> data = new HashMap<>();
        data.put("cartCount", cartService.count(currentUserId(session)));
        return data;
    }

    private Map<String, Object> response(CartService.CartResult result) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", result.success());
        data.put("message", result.message());
        data.put("cartCount", result.cartCount());
        return data;
    }

    private Integer currentUserId(HttpSession session) {
        Object value = session.getAttribute("userId");
        if (value instanceof Integer id) {
            return id;
        }
        return 1;
    }
}
