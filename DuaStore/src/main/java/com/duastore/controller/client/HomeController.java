package com.duastore.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");
        
        try {
            Integer userId = 2; // Tài khoản test Nguyễn Văn An
            
            // --- 1. LẤY SẢN PHẨM YÊU THÍCH ---
            String sql = "SELECT DISTINCT w.productId, p.tenSanPham, p.giaBan, p.hinhAnhHienThi " +
                         "FROM Wishlists w " +
                         "JOIN vw_ProductPrice p ON w.productId = p.id " +
                         "WHERE w.userId = ?";
            List<Map<String, Object>> myWishlist = jdbcTemplate.queryForList(sql, userId);
            model.addAttribute("myWishlist", myWishlist); 
            
            List<Integer> likedIds = jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId);
            model.addAttribute("likedIds", likedIds);
            
            // --- 2. CODE THÊM MỚI: LẤY SẢN PHẨM TRONG GIỎ HÀNG ---
            String cartSql = "SELECT c.productId, p.tenSanPham, p.giaBan, p.hinhAnhHienThi, c.soLuong " +
                             "FROM CartItems c " +
                             "JOIN vw_ProductPrice p ON c.productId = p.id " +
                             "WHERE c.userId = ?";
            List<Map<String, Object>> myCart = jdbcTemplate.queryForList(cartSql, userId);
            model.addAttribute("myCart", myCart); // Gửi danh sách giỏ hàng ra HTML

            // Tự động tính tổng số lượng để hiển thị lên Badge màu đỏ bên cạnh Icon chiếc túi
            String countSql = "SELECT SUM(soLuong) FROM CartItems WHERE userId = ?";
            Integer cartCount = jdbcTemplate.queryForObject(countSql, Integer.class, userId);
            model.addAttribute("cartCount", cartCount != null ? cartCount : 0);

        } catch (Exception e) {
            System.out.println("Lỗi đọc DB: " + e.getMessage());
        }

        return "view/client/index";
    }
}