package com.duastore.config;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Category;
import com.duastore.model.User;
import com.duastore.repository.CategoryRepository;
import com.duastore.service.client.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    private final CategoryRepository categoryRepository;
    private final CartService cartService;
    private final SecurityUtil securityUtil;

    @Value("${google.maps.api.key}")
    private String googleMapsApiKey;

    public GlobalControllerAdvice(CategoryRepository categoryRepository,
                                  CartService cartService,
                                  SecurityUtil securityUtil) {
        this.categoryRepository = categoryRepository;
        this.cartService = cartService;
        this.securityUtil = securityUtil;
    }

    @ModelAttribute("navCategories")
    public List<Category> navCategories() {
        try {
            return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByThuTuHienThiAscIdAsc();
        } catch (Exception e) {
            log.warn("Loi navCategories: {}", e.getMessage());
            return List.of();
        }
    }

    @ModelAttribute("googleMapsApiKey")
    public String googleMapsApiKey() {
        return googleMapsApiKey;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId == null) return 0;
            return cartService.count(userId);
        } catch (Exception e) {
            log.warn("Loi cartCount: {}", e.getMessage());
            return 0;
        }
    }

    @ModelAttribute("requestURI")
    public String getRequestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
