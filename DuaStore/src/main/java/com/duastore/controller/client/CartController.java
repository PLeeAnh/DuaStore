package com.duastore.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/gio-hang")
    public String viewCart(Model model) {
        model.addAttribute("title", "Giỏ hàng của bạn");
        Integer userId = 2; // Dùng chung tài khoản test

        try {
            // Lấy danh sách sản phẩm trong giỏ (tương tự như Popup)
            String sql = "SELECT c.productId, c.variantId, p.tenSanPham, v.tenBienThe, " +
                         "COALESCE(v.giaKhuyenMai, v.giaGoc) as giaBan, " +
                         "COALESCE(v.hinhAnh, p.hinhAnhChinh) as hinhAnhHienThi, c.soLuong " +
                         "FROM CartItems c " +
                         "JOIN Products p ON c.productId = p.id " +
                         "JOIN ProductVariants v ON c.variantId = v.id " +
                         "WHERE c.userId = ?";
            List<Map<String, Object>> cartItems = jdbcTemplate.queryForList(sql, userId);
            model.addAttribute("cartItems", cartItems);

            // Tính tổng tiền
            double total = 0;
            for (Map<String, Object> item : cartItems) {
                double price = ((Number) item.get("giaBan")).doubleValue();
                int qty = ((Number) item.get("soLuong")).intValue();
                total += price * qty;
            }
            model.addAttribute("totalPrice", total);

        } catch (Exception e) {
            System.out.println("Lỗi load trang giỏ hàng: " + e.getMessage());
        }

        return "view/client/cart/cart"; // Trỏ đúng về file cart.html của bạn
    }

    private Integer currentUserId(HttpSession session) {
        return 2;
    }
}