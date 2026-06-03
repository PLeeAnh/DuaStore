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
 * ★ AdminProductController — CRUD sản phẩm cho Admin
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Controller này quản lý các thao tác CRUD sản phẩm từ phía admin.
 * Tất cả route bắt đầu bằng /admin/san-pham.
 * 
 * View templates nằm trong: /WEB-INF/views/admin/product/
 * Sử dụng AdminProductService để xử lý nghiệp vụ.
 * 
 * ★ TODO [Lê Văn C]: @GetMapping("/admin/san-pham") — Danh sách
 *   - Hiển thị danh sách tất cả sản phẩm (phân trang)
 *   - Hiển thị trạng thái: còn hàng / hết hàng / ngừng bán
 *   - Có ô tìm kiếm + lọc theo danh mục
 *   - Trả về view: "admin/product/list"
 * 
 * ★ TODO [Lê Văn C]: @GetMapping("/admin/san-pham/them-moi") — Form thêm
 *   - Hiển thị form tạo sản phẩm mới
 *   - Load danh sách danh mục cho dropdown
 *   - Trả về view: "admin/product/form"
 *   - Đưa đối tượng Product (rỗng) vào Model attribute "product"
 * 
 * ★ TODO [Lê Văn C]: @PostMapping("/admin/san-pham/them-moi") — Xử lý thêm
 *   - Nhận dữ liệu từ form (Product + MultipartFile ảnh)
 *   - Validate dữ liệu (tên không trống, giá > 0, v.v.)
 *   - Upload ảnh qua FileUploadService (hoặc xử lý trực tiếp)
 *   - Lưu vào DB qua AdminProductService
 *   - Redirect về /admin/san-pham kèm flash message
 *   - BindingResult kiểm tra lỗi validate
 * 
 * ★ TODO [Lê Văn C]: @GetMapping("/admin/san-pham/sua/{id}") — Form sửa
 *   - Load sản phẩm theo id
 *   - Đưa vào Model attribute "product"
 *   - Hiển thị ảnh hiện tại
 *   - Trả về view: "admin/product/form"
 * 
 * ★ TODO [Lê Văn C]: @PostMapping("/admin/san-pham/sua/{id}") — Xử lý sửa
 *   - Nhận dữ liệu từ form + MultipartFile (có thể null)
 *   - Validate + upload ảnh mới nếu có
 *   - Update sản phẩm
 *   - Redirect về /admin/san-pham
 * 
 * ★ TODO [Lê Văn C]: @PostMapping("/admin/san-pham/xoa/{id}") — Xóa
 *   - Xóa mềm (cập nhật status = 0) hoặc xóa cứng
 *   - Kiểm tra sản phẩm có trong đơn hàng không trước khi xóa
 *   - Redirect về /admin/san-pham
 * 
 * ⚠ Lưu ý:
 *   - Sử dụng @Valid cho validation (Jakarta Bean Validation)
 *   - Xử lý upload ảnh qua MultipartFile
 *   - Sử dụng RedirectAttributes cho flash messages
 *   - Phân biệt thêm mới và chỉnh sửa trong cùng form
 */
@Controller
@RequestMapping("/admin/san-pham")
public class AdminProductController {
    
}
