package com.duastore.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CartWishlistApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. LƯU / XÓA YÊU THÍCH VÀO CSDL
    @PostMapping("/wishlist/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = 2; // Tạm thời dùng tài khoản Nguyễn Văn An (id=2)
        Integer productId = payload.get("productId");
        
        String checkSql = "SELECT COUNT(*) FROM Wishlists WHERE userId = ? AND productId = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, productId);
        
        if (count != null && count > 0) {
            jdbcTemplate.update("DELETE FROM Wishlists WHERE userId = ? AND productId = ?", userId, productId);
        } else {
            jdbcTemplate.update("INSERT INTO Wishlists (userId, productId) VALUES (?, ?)", userId, productId);
        }
        
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // 2. THÊM VÀO GIỎ HÀNG
    @PostMapping("/cart/add-popup")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = 2; 
        Integer productId = payload.get("productId");
        Integer variantId = payload.get("variantId");
        Integer quantity = payload.get("quantity");
        if(quantity == null) quantity = 1;

        if (variantId == null) {
            try {
                String sqlVar = "SELECT TOP 1 id FROM ProductVariants WHERE productId = ? AND isDefault = 1";
                variantId = jdbcTemplate.queryForObject(sqlVar, Integer.class, productId);
            } catch(Exception e) {
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }
        }

        String checkSql = "SELECT COUNT(*) FROM CartItems WHERE userId = ? AND variantId = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, variantId);
        
        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE CartItems SET soLuong = soLuong + ? WHERE userId = ? AND variantId = ?", quantity, userId, variantId);
        } else {
            jdbcTemplate.update("INSERT INTO CartItems (userId, productId, variantId, soLuong) VALUES (?, ?, ?, ?)", userId, productId, variantId, quantity);
        }
        
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // 3. XÓA SẢN PHẨM KHỎI GIỎ HÀNG (Sửa thành xóa theo variantId)
    @PostMapping("/cart/remove-item")
    public ResponseEntity<Map<String, Object>> removeCartItem(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = 2; 
        Integer variantId = payload.get("variantId"); // <-- Đổi thành lấy variantId

        try {
            jdbcTemplate.update("DELETE FROM CartItems WHERE userId = ? AND variantId = ?", userId, variantId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 4. API CẬP NHẬT SỐ LƯỢNG (Sửa thành cập nhật theo variantId)
    @PostMapping("/cart/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody Map<String, Integer> payload) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = 2; 
        Integer variantId = payload.get("variantId"); // <-- Đổi thành lấy variantId
        Integer soLuong = payload.get("soLuong");

        try {
            jdbcTemplate.update("UPDATE CartItems SET soLuong = ? WHERE userId = ? AND variantId = ?", soLuong, userId, variantId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi cập nhật CSDL: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}