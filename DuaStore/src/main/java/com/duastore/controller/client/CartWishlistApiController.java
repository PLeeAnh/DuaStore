package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CartWishlistApiController {

    private final JdbcTemplate jdbcTemplate;
    private final SecurityUtil securityUtil;

    public CartWishlistApiController(JdbcTemplate jdbcTemplate, SecurityUtil securityUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.securityUtil = securityUtil;
    }

    private Integer getUserId() {
        Integer id = securityUtil.getCurrentUserId();
        if (id == null) throw new RuntimeException("Vui long dang nhap");
        return id;
    }

    @PostMapping("/wishlist/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer productId = payload.get("productId");
            if (productId == null) throw new RuntimeException("Thiếu thông tin sản phẩm");
            String checkSql = "SELECT COUNT(*) FROM Wishlists WHERE userId = ? AND productId = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, productId);
            if (count != null && count > 0) {
                jdbcTemplate.update("DELETE FROM Wishlists WHERE userId = ? AND productId = ?", userId, productId);
            } else {
                jdbcTemplate.update("INSERT INTO Wishlists (userId, productId) VALUES (?, ?)", userId, productId);
            }
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cart/add-popup")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer productId = payload.get("productId");
            Integer variantId = payload.get("variantId");
            Integer quantity = payload.get("quantity");
            if (quantity == null) quantity = 1;
            if (quantity < 1 || quantity > 99) throw new RuntimeException("Số lượng không hợp lệ");

            if (variantId == null) {
                String sqlVar = "SELECT TOP 1 id FROM ProductVariants WHERE productId = ? AND isDefault = 1";
                variantId = jdbcTemplate.queryForObject(sqlVar, Integer.class, productId);
            }

            String checkSql = "SELECT COUNT(*) FROM CartItems WHERE userId = ? AND variantId = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, variantId);

            if (count != null && count > 0) {
                jdbcTemplate.update("UPDATE CartItems SET soLuong = soLuong + ? WHERE userId = ? AND variantId = ?", quantity, userId, variantId);
            } else {
                jdbcTemplate.update("INSERT INTO CartItems (userId, productId, variantId, soLuong) VALUES (?, ?, ?, ?)", userId, productId, variantId, quantity);
            }
            response.put("success", true);
            String countSql = "SELECT COUNT(*) FROM CartItems WHERE userId = ?";
            Integer cartCount = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
            response.put("cartCount", cartCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/cart/remove-item")
    public ResponseEntity<Map<String, Object>> removeCartItem(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer userId = getUserId();
            Integer variantId = payload.get("variantId");

            String getProductSql = "SELECT productId FROM CartItems WHERE userId = ? AND variantId = ?";
            Integer productId = jdbcTemplate.queryForObject(getProductSql, Integer.class, userId, variantId);

            jdbcTemplate.update("DELETE FROM CartItems WHERE userId = ? AND variantId = ?", userId, variantId);
            response.put("success", true);

            String countSql = "SELECT COUNT(*) FROM CartItems WHERE userId = ?";
            Integer cartCount = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
            response.put("cartCount", cartCount);

            Integer remaining = 0;
            if (productId != null) {
                String remainingSql = "SELECT COUNT(*) FROM CartItems WHERE userId = ? AND productId = ?";
                remaining = jdbcTemplate.queryForObject(remainingSql, Integer.class, userId, productId);
            }
            response.put("remainingItems", remaining);

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
            if (variantId == null) throw new RuntimeException("Thiếu thông tin biến thể");
            if (soLuong == null || soLuong < 1 || soLuong > 99) throw new RuntimeException("Số lượng không hợp lệ");
            jdbcTemplate.update("UPDATE CartItems SET soLuong = ? WHERE userId = ? AND variantId = ?", soLuong, userId, variantId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Loi cap nhat CSDL: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}