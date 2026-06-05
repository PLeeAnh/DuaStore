package com.duastore.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

// Đổi tên class để không bị xung đột với file cũ của dự án
@ControllerAdvice(basePackages = "com.duastore.controller.client")
public class ClientNavbarAdvice {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Hàm này sẽ TỰ ĐỘNG CHẠY trước khi load bất kỳ trang web nào của Client
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            Integer userId = 2; // Vẫn dùng tài khoản test Nguyễn Văn An (id=2)

            // 1. Tự động bơm dữ liệu Giỏ Hàng vào Navbar (Đã sửa lỗi trùng lặp & lấy đúng giá biến thể)
            String cartSql = "SELECT c.productId, c.variantId, p.tenSanPham, v.tenBienThe, " +
                             "COALESCE(v.giaKhuyenMai, v.giaGoc) as giaBan, " +
                             "COALESCE(v.hinhAnh, p.hinhAnhChinh) as hinhAnhHienThi, c.soLuong " +
                             "FROM CartItems c " +
                             "JOIN Products p ON c.productId = p.id " +
                             "JOIN ProductVariants v ON c.variantId = v.id " +
                             "WHERE c.userId = ?";
            List<Map<String, Object>> myCart = jdbcTemplate.queryForList(cartSql, userId);
            model.addAttribute("myCart", myCart);

            String countSql = "SELECT SUM(soLuong) FROM CartItems WHERE userId = ?";
            Integer cartCount = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
            model.addAttribute("cartCount", cartCount != null ? cartCount : 0);

            // 2. Tự động bơm dữ liệu Yêu Thích vào Navbar
            String wishSql = "SELECT DISTINCT w.productId, p.tenSanPham, p.giaBan, p.hinhAnhHienThi " +
                             "FROM Wishlists w JOIN vw_ProductPrice p ON w.productId = p.id WHERE w.userId = ?";
            List<Map<String, Object>> myWishlist = jdbcTemplate.queryForList(wishSql, userId);
            model.addAttribute("myWishlist", myWishlist);

            // Bơm danh sách các ID đã thích để bôi đỏ trái tim
            List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
            model.addAttribute("likedIds", likedIds);

        } catch (Exception e) {
            System.out.println("Lỗi ClientNavbarAdvice: " + e.getMessage());
        }
    }
}