/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.service.client;

import com.duastore.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ★ ProductService — Service sản phẩm cho Client
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Service cung cấp các phương thức phục vụ client (người dùng cuối).
 * Chỉ trả về các sản phẩm có status = 1 (active).
 * 
 * Inject ProductRepository để truy vấn dữ liệu.
 * Các kết quả luôn được lọc để chỉ hiển thị sản phẩm đang bán.
 * 
 * ★ TODO [Trần Thị B]: Tìm kiếm sản phẩm
 *   - Product getProductById(Long id) — Lấy sản phẩm theo id (chỉ active)
 *   - Product getProductBySlug(String slug) — Lấy sản phẩm theo slug (SEO)
 *   - Page<Product> searchProducts(String keyword, Pageable pageable) — Tìm kiếm theo tên
 * 
 * ★ TODO [Trần Thị B]: Danh sách sản phẩm
 *   - Page<Product> getProductsByCategory(Long categoryId, Pageable pageable)
 *     — Sản phẩm theo danh mục (active, phân trang)
 *   - Page<Product> getAllActiveProducts(Pageable pageable) — Tất cả sản phẩm đang bán
 * 
 * ★ TODO [Trần Thị B]: Sản phẩm đặc biệt
 *   - List<Product> getFeaturedProducts(int limit) — Sản phẩm nổi bật
 *   - List<Product> getNewArrivals(int limit) — Sản phẩm mới nhất
 *   - List<Product> getBestSellingProducts(int limit) — Sản phẩm bán chạy
 *   - List<Product> getDiscountedProducts(int limit) — Sản phẩm đang giảm giá
 * 
 * ★ TODO [Trần Thị B]: Sản phẩm liên quan
 *   - List<Product> getRelatedProducts(Long productId, int limit)
 *     — Sản phẩm cùng danh mục, loại trừ sản phẩm hiện tại
 * 
 * ★ TODO [Trần Thị B]: Lọc và sắp xếp
 *   - Page<Product> filterProducts(Long categoryId, String search,
 *     BigDecimal minPrice, BigDecimal maxPrice, String sortBy, Pageable pageable)
 *     — Lọc theo danh mục, giá, tìm kiếm, sắp xếp
 * 
 * ⚠ Lưu ý:
 *   - Đánh dấu @Service
 *   - @Transactional(readOnly = true) — Chỉ đọc dữ liệu
 *   - Tất cả query phải có điều kiện status = 1
 *   - Xử lý trường hợp không tìm thấy (throw RuntimeException hoặc trả về Optional)
 *   - Có thể cache các danh sách ít thay đổi (featured, new arrivals)
 */
@Service
@Transactional(readOnly = true)
public class ProductService {
    
}
