package com.duastore.config;

import com.duastore.model.Category;
import com.duastore.repository.CategoryRepository;
import com.duastore.service.client.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * ★ GlobalControllerAdvice — inject dữ liệu GLOBAL vào mọi template
 *  @ModelAttribute: navCategories (dropdown), cartCount (badge), requestURI (active nav)
 *  Backend: inject CategoryService + CartService, xóa placeholder
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final CategoryRepository categoryRepository;
    private final CartService cartService;

    // ── Uncomment khi NHD hoàn thành CartService ──
    // private final CartService cartService;

    // ── Constructor injection (dùng khi uncomment 2 dòng trên) ──
    public GlobalControllerAdvice(CategoryRepository categoryRepository, CartService cartService) {
        this.categoryRepository = categoryRepository;
        this.cartService = cartService;
    }

    /**
     * ★ Trả về danh sách danh mục gốc (parentId = null) cho navbar dropdown.
     *   Placeholder hiện tại trả về danh sách rỗng.
     *   Khi có CategoryService, đổi thành:
     *     return categoryService.findRootCategories();
     */
    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
    }

    /**
     * ★ Trả về số sản phẩm trong giỏ hàng của người dùng đang đăng nhập.
     *   Placeholder hiện tại trả về 0.
     *   Khi có CartService + Security, đổi thành:
     *     Long userId = getCurrentUserId();
     *     return cartService.countByUserId(userId);
     */
    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        // TODO: lấy userId từ SecurityContextHolder, rồi:
        // return cartService.countByUserId(userId);
        Object value = session.getAttribute("userId");
        Integer userId = value instanceof Integer id ? id : 1;
        return cartService.count(userId);
    }
    
    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
