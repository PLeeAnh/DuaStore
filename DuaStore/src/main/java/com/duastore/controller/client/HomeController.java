package com.duastore.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Không cần khai báo JdbcTemplate ở đây nữa vì GlobalControllerAdvice đã làm thay rồi

    @GetMapping("/")
    public String home(Model model) {
        
        model.addAttribute("title", "Trang chủ");
        
        // Dữ liệu myCart, cartCount, myWishlist, likedIds 
        // đều đã được Spring Boot TỰ ĐỘNG BƠM ngầm vào model rồi!
        
        // (Sau này nếu bạn muốn lấy thêm danh sách Sản phẩm nổi bật hoặc Slider
        // để truyền ra ngoài trang chủ thì bạn mới cần viết thêm code truy vấn ở đây)

        return "view/client/index";
    }
}