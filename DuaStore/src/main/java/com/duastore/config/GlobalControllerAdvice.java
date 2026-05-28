package com.duastore.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

/**
 * ★ GlobalControllerAdvice — inject dữ liệu GLOBAL vào mọi template
 *  @ModelAttribute: navCategories (dropdown), cartCount (badge), requestURI (active nav)
 *  Backend: inject CategoryService + CartService, xóa placeholder
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    // ── Uncomment khi TK hoàn thành CategoryService ──
    // private final CategoryService categoryService;

    // ── Uncomment khi NHD hoàn thành CartService ──
    // private final CartService cartService;

    // ── Constructor injection (dùng khi uncomment 2 dòng trên) ──
    // public GlobalControllerAdvice(CategoryService categoryService,
    //                               CartService cartService) {
    //     this.categoryService = categoryService;
    //     this.cartService     = cartService;
    // }

    /**
     * ★ Trả về danh sách danh mục gốc (parentId = null) cho navbar dropdown.
     *   Placeholder hiện tại trả về danh sách rỗng.
     *   Khi có CategoryService, đổi thành:
     *     return categoryService.findRootCategories();
     */
    @ModelAttribute("navCategories")
    public List<Object> navCategories() {
        // TODO: return categoryService.findRootCategories();
        return Collections.emptyList();
    }

    /**
     * ★ Trả về số sản phẩm trong giỏ hàng của người dùng đang đăng nhập.
     *   Placeholder hiện tại trả về 0.
     *   Khi có CartService + Security, đổi thành:
     *     Long userId = getCurrentUserId();
     *     return cartService.countByUserId(userId);
     */
    @ModelAttribute("cartCount")
    public int cartCount() {
        // TODO: lấy userId từ SecurityContextHolder, rồi:
        // return cartService.countByUserId(userId);
        return 0;
    }
    
    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
