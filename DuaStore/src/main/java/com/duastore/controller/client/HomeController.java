package com.duastore.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ★ HomeController — Xử lý trang chủ client
 *  GET / → home() → title="Trang chủ" → view/client/index
 *  Backend: thêm model attributes (featuredProducts, latestPosts...)
 *           và các @GetMapping cho about/contact
 */
@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");
        return "view/client/index";
    }
    
}
