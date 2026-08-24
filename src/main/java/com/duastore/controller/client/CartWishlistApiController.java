package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.client.CartService;
import com.duastore.service.client.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
/**
 * Controller xử lý các request HTTP liên quan tới giỏ hàng, danh sách yêu thích.
 */
public class CartWishlistApiController {

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;

    public CartWishlistApiController(CartService cartService, WishlistService wishlistService, SecurityUtil securityUtil) {
        this.cartService = cartService;
        this.wishlistService = wishlistService;
        this.securityUtil = securityUtil;
    }

    private Integer getUserId() {
        Integer id = securityUtil.getCurrentUserId();
        if (id == null) {
            throw new RuntimeException("Vui long dang nhap");
        }
        return id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/wishlist/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer productId = payload.get("productId");
            if (productId == null) {
                throw new RuntimeException("Thiếu thông tin sản phẩm");
            }
            boolean isLiked = wishlistService.toggle(userId, productId);
            response.put("success", true);
            response.put("isLiked", isLiked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/add-popup")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer productId = payload.get("productId");
            Integer variantId = payload.get("variantId");
            Integer quantity = payload.get("quantity");

            if (variantId == null) {
                var opt = cartService.findDefaultVariant(productId);
                if (opt.isEmpty()) {
                    throw new RuntimeException("Sản phẩm chưa có biến thể");
                }
                variantId = opt.get().getId();
            }
            CartService.CartResult result = cartService.add(userId, variantId, quantity);
            if (!result.success()) {
                throw new RuntimeException(result.message());
            }

            response.put("success", true);
            response.put("cartCount", result.cartCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* Khách chưa đăng nhập được thêm giỏ hàng lưu tạm ở localStorage (client-side);
       khi đăng nhập xong, client gọi API này 1 lần để gộp giỏ hàng tạm đó vào
       giỏ hàng thật trong DB của tài khoản, tránh mất giỏ hàng sau khi đăng nhập. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/merge-guest")
    public ResponseEntity<Map<String, Object>> mergeGuestCart(@RequestBody List<Map<String, Integer>> items) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            int merged = 0;
            if (items != null) {
                for (Map<String, Integer> item : items) {
                    Integer variantId = item.get("variantId");
                    Integer quantity = item.get("quantity");
                    if (variantId == null) continue;
                    if (quantity == null || quantity < 1) quantity = 1;
                    CartService.CartResult r = cartService.add(userId, variantId, quantity);
                    if (r.success()) merged++;
                }
            }
            response.put("success", true);
            response.put("merged", merged);
            response.put("cartCount", cartService.count(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/cart/remove-item")
    public ResponseEntity<Map<String, Object>> removeCartItem(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer variantId = payload.get("variantId");
            if (variantId == null) {
                throw new RuntimeException("Thiếu thông tin biến thể");
            }
            cartService.removeByVariantId(userId, variantId);
            response.put("success", true);
            response.put("cartCount", cartService.count(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cart/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody Map<String, Integer> payload) {

        Map<String, Object> response = new HashMap<>();

        try {

            Integer userId = getUserId();
            Integer variantId = payload.get("variantId");
            Integer soLuong = payload.get("soLuong");

            if (variantId == null) {
                throw new RuntimeException("Thiếu thông tin biến thể");
            }

            if (soLuong == null || soLuong < 1) {
                throw new RuntimeException("Số lượng không hợp lệ");
            }

            CartService.CartResult result
                    = cartService.updateQuantityByVariantId(
                            userId,
                            variantId,
                            soLuong
                    );

            response.put("success", result.success());
            response.put("message", result.message());
            response.put("cartCount", result.cartCount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
