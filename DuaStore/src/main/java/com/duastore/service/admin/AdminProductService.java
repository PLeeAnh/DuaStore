/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.service.admin;

import com.duastore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ★ AdminProductService — Service CRUD sản phẩm cho Admin
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Service cung cấp các phương thức CRUD cho sản phẩm từ phía admin.
 * Inject ProductRepository và ProductVariantRepository để thao tác DB.
 * 
 * Xử lý các nghiệp vụ: tạo/sửa/xóa sản phẩm, upload ảnh, sinh slug, quản lý trạng thái.
 * 
 * ★ TODO [Lê Văn C]: CRUD cơ bản
 *   - Page<Product> getAllProducts(Pageable pageable) — Danh sách có phân trang
 *   - Product getProductById(Long id) — Lấy theo id (ném exception nếu không tìm thấy)
 *   - Product saveProduct(Product product) — Lưu mới hoặc cập nhật
 *   - void deleteProduct(Long id) — Xóa (cập nhật status = 0)
 * 
 * ★ TODO [Lê Văn C]: Tìm kiếm và lọc
 *   - Page<Product> searchProducts(String keyword, Pageable pageable)
 *     — Tìm kiếm theo tên sản phẩm
 *   - Page<Product> getProductsByCategory(Long categoryId, Pageable pageable)
 *     — Lọc theo danh mục
 *   - Page<Product> filterProducts(String keyword, Long categoryId, Integer status, Pageable pageable)
 *     — Lọc kết hợp
 * 
 * ★ TODO [Lê Văn C]: Xử lý slug
 *   - String generateSlug(String name) — Sinh slug từ tên (bỏ dấu, replace space, lowercase)
 *   - String generateUniqueSlug(String name) — Đảm bảo slug không trùng
 *   - Cập nhật slug khi tên sản phẩm thay đổi
 * 
 * ★ TODO [Lê Văn C]: Xử lý ảnh
 *   - String saveImage(MultipartFile file) — Lưu ảnh vào thư mục upload, trả về đường dẫn
 *   - void deleteImage(String imagePath) — Xóa ảnh cũ
 *   - List<String> saveImages(List<MultipartFile> files) — Lưu nhiều ảnh
 * 
 * ★ TODO [Lê Văn C]: Validation
 *   - validateProduct(Product product, BindingResult result) — Kiểm tra dữ liệu đầu vào
 *   - Kiểm tra tên không trống, giá > 0
 * 
 * ⚠ Lưu ý:
 *   - Đánh dấu @Service
 *   - @Transactional cho các method write
 *   - Sử dụng try-catch với RuntimeException khi cần
 *   - Khi xóa sản phẩm, cần kiểm tra ràng buộc với bảng khác (order_detail)
 *   - Sinh slug tự động để SEO-friendly
 */
@Service
@Transactional
public class AdminProductService {
    
}
