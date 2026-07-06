package com.duastore.controller.admin;

import com.duastore.model.Banner;
import com.duastore.repository.BannerRepository;
import com.duastore.repository.CategoryRepository;
import com.duastore.repository.PostsRepository;
import com.duastore.repository.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/homepage")
// Controller của dashboard quản lý Homepage. Màn hình này chỉ tổng hợp số liệu và
// đường dẫn sang các module quản lý nội dung; hiện code gọi Repository trực tiếp
// không có một HomepageService riêng ở phía quản trị
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

        // Lấy toàn bộ banner (kể cả chưa active) vì đây là thống kê dành cho quản trị viên.
        List<Banner> banners = bannerRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc();
        model.addAttribute("banners", banners);
        model.addAttribute("bannerCount", banners.size());

        // count() đếm toàn bộ bản ghi, không chỉ bản ghi đang hoạt động/nổi bật.
        long productCount = productRepository.count();
        model.addAttribute("productCount", productCount);

        long categoryCount = categoryRepository.count();
        model.addAttribute("categoryCount", categoryCount);

        long postCount = postsRepository.count();
        model.addAttribute("postCount", postCount);

        // Các thuộc tính Model phía trên được Thymeleaf dùng để dựng thẻ thống kê.
        return "view/admin/homepage/dashboard";
    }
}
