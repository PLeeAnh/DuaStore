/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ★ WebMvcConfig — Cấu hình Spring MVC
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Class này triển khai WebMvcConfigurer để tùy chỉnh cấu hình Spring MVC.
 * Cần đánh dấu @Configuration và implements WebMvcConfigurer,
 * sau đó override các phương thức addViewControllers, addResourceHandlers, addCorsMappings.
 * 
 * ★ TODO [Nguyễn Văn A]: Cấu hình view controller
 *   - Đăng ký view controller cho route "/" → "index"
 *   - Đăng ký view controller cho route "/dang-nhap" → "login"
 *   - Đăng ký view controller cho route "/dang-ky" → "register"
 * 
 * ★ TODO [Nguyễn Văn A]: Cấu hình static resources
 *   - /resources/**, /static/**, /public/**, /custom/**
 *   - Phục vụ các tài nguyên tĩnh như CSS, JS, images
 * 
 * ★ TODO [Nguyễn Văn A]: Cấu hình CORS
 *   - Cho phép tất cả origins trong môi trường dev
 *   - Hạn chế origins trong production
 *   - Cho phép các methods: GET, POST, PUT, DELETE, OPTIONS
 * 
 * ⚠ Lưu ý: Cần đảm bảo @Configuration + implements WebMvcConfigurer
 *           @EnableWebMvc nếu cần override hoàn toàn (thường không cần)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
}
