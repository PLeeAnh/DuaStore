package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice(basePackages = "com.duastore.controller.client")
public class ClientNavbarAdvice {

    private final JdbcTemplate jdbcTemplate;
    private final SecurityUtil securityUtil;

    public ClientNavbarAdvice(JdbcTemplate jdbcTemplate, SecurityUtil securityUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("myCart", List.of());
        model.addAttribute("myWishlist", List.of());
        model.addAttribute("likedIds", List.of());

        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId == null) return;

            String cartSql = "SELECT c.productId, c.variantId, p.tenSanPham, v.tenBienThe, " +
                             "COALESCE(v.giaKhuyenMai, v.giaGoc) as giaBan, " +
                             "COALESCE(v.hinhAnh, p.hinhAnhChinh) as hinhAnhHienThi, c.soLuong " +
                             "FROM CartItems c " +
                             "JOIN Products p ON c.productId = p.id " +
                             "JOIN ProductVariants v ON c.variantId = v.id " +
                             "WHERE c.userId = ?";
            model.addAttribute("myCart", jdbcTemplate.queryForList(cartSql, userId));

            String wishSql = "SELECT DISTINCT w.productId, p.tenSanPham, p.giaBan, p.hinhAnhHienThi " +
                             "FROM Wishlists w JOIN vw_ProductPrice p ON w.productId = p.id WHERE w.userId = ?";
            model.addAttribute("myWishlist", jdbcTemplate.queryForList(wishSql, userId));

            model.addAttribute("likedIds",
                jdbcTemplate.queryForList("SELECT productId FROM Wishlists WHERE userId = ?", Integer.class, userId));

        } catch (Exception e) {
            System.out.println("Loi ClientNavbarAdvice: " + e.getMessage());
        }
    }
    
}