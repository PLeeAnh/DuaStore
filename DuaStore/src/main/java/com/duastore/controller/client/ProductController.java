/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ★ ProductController — Controller sản phẩm cho Client
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Controller này xử lý các request từ phía client (người dùng cuối)
 * liên quan đến sản phẩm: xem danh sách, chi tiết, tìm kiếm, lọc theo danh mục.
 * 
 * Các view template nằm trong: /WEB-INF/views/client/product/
 * Sử dụng ProductService để lấy dữ liệu.
 * 
 * ★ TODO [Trần Thị B]: @GetMapping("/san-pham") — Danh sách sản phẩm
 *   - Hỗ trợ phân trang (page, size params)
 *   - Hỗ trợ lọc theo danh mục (@RequestParam danhMuc)
 *   - Hỗ trợ tìm kiếm (@RequestParam search)
 *   - Hỗ trợ sắp xếp (price-asc, price-desc, newest, best-selling)
 *   - Trả về view: "client/product/list"
 *   - Đưa dữ liệu vào Model
 * 
 * ★ TODO [Trần Thị B]: @GetMapping("/san-pham/{id}") — Chi tiết sản phẩm
 *   - Lấy Product theo id hoặc slug
 *   - Lấy danh sách ProductVariant (size, color, stock, price)
 *   - Lấy sản phẩm liên quan (cùng danh mục)
 *   - Trả về view: "client/product/detail"
 *   - Xử lý trường hợp không tìm thấy (404)
 * 
 * ★ TODO [Trần Thị B]: Các lọc bổ sung
 *   - @RequestParam(required = false) Long danhMuc
 *   - @RequestParam(required = false) String search
 *   - @RequestParam(defaultValue = "newest") String sort
 *   - @RequestParam(defaultValue = "1") int page
 *   - @RequestParam(defaultValue = "12") int size
 * 
 * ⚠ Lưu ý:
 *   - Sử dụng @Controller (không phải @RestController)
 *   - Inject ProductService, CategoryService (nếu có)
 *   - Tất cả mapping bắt đầu bằng /san-pham
 *   - Cần xử lý slug để SEO-friendly URLs
 */
@Controller
@RequestMapping("/san-pham")
public class ProductController {
    
}
