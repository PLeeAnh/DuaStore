package com.duastore.controller.admin;

import com.duastore.model.Banner;
import com.duastore.repository.BannerRepository;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.PostsRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/homepage")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới trang chủ.
 */
public class AdminHomepageController {

    private final BannerRepository bannerRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PostsRepository postsRepository;

    public AdminHomepageController(BannerRepository bannerRepository,
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            PostsRepository postsRepository) {
        this.bannerRepository = bannerRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.postsRepository = postsRepository;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).HOMEPAGE_READ)")
    public String homepage(Model model) {
        model.addAttribute("title", "homepage");

        List<Banner> banners = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc();
        model.addAttribute("banners", banners);
        model.addAttribute("bannerCount", banners.size());

        long productCount = productRepository.count();
        model.addAttribute("productCount", productCount);

        long categoryCount = categoryRepository.count();
        model.addAttribute("categoryCount", categoryCount);

        long postCount = postsRepository.count();
        model.addAttribute("postCount", postCount);

        return "view/admin/homepage/dashboard";
    }
}
