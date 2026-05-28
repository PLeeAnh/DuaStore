/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ★ AdminVariantController — CRUD biến thể sản phẩm cho Admin
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Controller này quản lý các biến thể (size, màu sắc, giá riêng, tồn kho)
 * của từng sản phẩm. Biến thể cho phép một sản phẩm có nhiều tùy chọn.
 * 
 * Tất cả route bắt đầu bằng /admin/san-pham/{productId}/bien-the
 * View templates nằm trong: /WEB-INF/views/admin/variant/
 * Sử dụng AdminVariantService để xử lý nghiệp vụ.
 * 
 * ★ TODO [Phạm Văn D]: @GetMapping("/admin/san-pham/{productId}/bien-the") — Danh sách
 *   - Hiển thị danh sách biến thể của một sản phẩm
 *   - Hiển thị thông tin: size, màu sắc, giá, tồn kho, trạng thái
 *   - Cho phép thêm mới / sửa / xóa ngay trên bảng (AJAX) hoặc form riêng
 *   - Trả về view: "admin/variant/list"
 * 
 * ★ TODO [Phạm Văn D]: @GetMapping("/admin/san-pham/{productId}/bien-the/them-moi") — Form thêm
 *   - Form thêm biến thể mới
 *   - Các trường: size, color, price (mặc định = giá sản phẩm), stock, status
 *   - Trả về view: "admin/variant/form"
 * 
 * ★ TODO [Phạm Văn D]: @PostMapping("/admin/san-pham/{productId}/bien-the/them-moi") — Xử lý thêm
 *   - Validate dữ liệu (price > 0, stock >= 0)
 *   - Kiểm tra trùng lặp (size + color đã tồn tại cho product này chưa)
 *   - Lưu qua AdminVariantService
 *   - Redirect về /admin/san-pham/{productId}/bien-the
 * 
 * ★ TODO [Phạm Văn D]: @GetMapping("/admin/san-pham/{productId}/bien-the/sua/{id}") — Form sửa
 *   - Load biến thể theo id
 *   - Điền sẵn dữ liệu vào form
 *   - Trả về view: "admin/variant/form"
 * 
 * ★ TODO [Phạm Văn D]: @PostMapping("/admin/san-pham/{productId}/bien-the/sua/{id}") — Xử lý sửa
 *   - Update biến thể
 *   - Redirect về danh sách
 * 
 * ★ TODO [Phạm Văn D]: @PostMapping("/admin/san-pham/{productId}/bien-the/xoa/{id}") — Xóa
 *   - Xóa biến thể
 *   - Redirect về danh sách
 * 
 * ⚠ Lưu ý:
 *   - Một sản phẩm có thể có nhiều biến thể (size S, M, L — màu đỏ, xanh...)
 *   - Giá biến thể có thể khác giá sản phẩm gốc (tính năng nâng cao)
 *   - Cần kiểm tra tồn kho trước khi cho phép đặt hàng
 *   - Có thể implement quick-edit (AJAX) để cập nhật stock nhanh
 */
@Controller
@RequestMapping("/admin/san-pham/{productId}/bien-the")
public class AdminVariantController {
    
}
