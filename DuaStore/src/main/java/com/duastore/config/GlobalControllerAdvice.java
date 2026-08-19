package com.duastore.config;

import com.duastore.config.security.SecurityUtil;
import com.duastore.dto.NavMenuCategory;
import com.duastore.model.Category;
import com.duastore.model.User;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.service.client.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);
    private static final int MEGA_MENU_PRODUCT_LIMIT = 5;

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final SecurityUtil securityUtil;

    public GlobalControllerAdvice(CategoryRepository categoryRepository,
            ProductRepository productRepository,
            CartService cartService,
            SecurityUtil securityUtil) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }

    // Cây danh mục cho mega menu desktop (kiểu sidebar columns như harum.io):
    // mỗi danh mục cha = 1 mục cấp cao nhất, panel hiển thị cột cho từng danh mục con,
    // mỗi cột liệt kê sản phẩm mới nhất (có ảnh) của danh mục con đó.
    @ModelAttribute("navCategories")
    public List<NavMenuCategory> navCategories() {
        List<NavMenuCategory> result = new ArrayList<>();
        try {
            for (Category parent : categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc()) {
                NavMenuCategory node = new NavMenuCategory();
                node.setId(parent.getId());
                node.setTenDanhMuc(parent.getTenDanhMuc());
                List<Category> childCats = categoryRepository.findByParentIdAndIsActiveTrueOrderByThuTuHienThiAscIdAsc(parent.getId());
                if (childCats.isEmpty()) {
                    node.setProducts(productRepository
                            .findByDanhMucIdAndIsActiveTrueOrderByNgayTaoDesc(parent.getId(), PageRequest.of(0, MEGA_MENU_PRODUCT_LIMIT)));
                } else {
                    for (Category child : childCats) {
                        NavMenuCategory childNode = new NavMenuCategory();
                        childNode.setId(child.getId());
                        childNode.setTenDanhMuc(child.getTenDanhMuc());
                        childNode.setProducts(productRepository
                                .findByDanhMucIdAndIsActiveTrueOrderByNgayTaoDesc(child.getId(), PageRequest.of(0, MEGA_MENU_PRODUCT_LIMIT)));
                        node.getChildren().add(childNode);
                    }
                }
                result.add(node);
            }
        } catch (Exception e) {
            log.warn("Loi build navCategories: {}", e.getMessage());
        }
        return result;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return 0;
        }
        return cartService.count(userId);
    }

    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
