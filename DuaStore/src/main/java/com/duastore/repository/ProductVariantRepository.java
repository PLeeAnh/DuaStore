/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.repository;

import com.duastore.model.ProductVariant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ★ ProductVariantRepository — Repository cho ProductVariant entity
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Repository interface cho entity ProductVariant.
 * Kế thừa JpaRepository để có sẵn CRUD + phân trang.
 * 
 * Các phương thức thường dùng: lấy biến thể theo sản phẩm, theo trạng thái,
 * kiểm tra tồn kho, tìm theo size/color.
 * 
 * ★ TODO [Nguyễn Thị E]: Tìm biến thể theo sản phẩm
 *   - List<ProductVariant> findByProductId(Long productId)
 *   - List<ProductVariant> findByProductIdAndStatus(Long productId, Integer status)
 * 
 * ★ TODO [Nguyễn Thị E]: Tìm biến thể theo size/color
 *   - List<ProductVariant> findByProductIdAndSize(Long productId, String size)
 *   - List<ProductVariant> findByProductIdAndColor(Long productId, String color)
 *   - ProductVariant findByProductIdAndSizeAndColor(Long productId, String size, String color)
 *     — Dùng để kiểm tra trùng lặp khi thêm/sửa biến thể
 * 
 * ★ TODO [Nguyễn Thị E]: Kiểm tra tồn kho
 *   - @Query("SELECT COUNT(v) FROM ProductVariant v WHERE v.product.id = :productId AND v.stock > 0 AND v.status = 1")
 *     long countInStockByProductId(@Param("productId") Long productId)
 *   - List<ProductVariant> findByProductIdAndStockGreaterThanAndStatus(Long productId, Integer minStock, Integer status)
 * 
 * ★ TODO [Nguyễn Thị E]: Xóa biến thể theo sản phẩm
 *   - void deleteByProductId(Long productId)
 *   — Dùng khi xóa sản phẩm (nếu cascade chưa đủ)
 * 
 * ★ TODO [Nguyễn Thị E]: Cập nhật tồn kho
 *   - @Modifying
 *     @Query("UPDATE ProductVariant v SET v.stock = :stock WHERE v.id = :id")
 *     int updateStock(@Param("id") Long id, @Param("stock") Integer stock)
 * 
 * ⚠ Lưu ý:
 *   - Tất cả query cho client cần thêm status = 1 (chỉ biến thể đang bán)
 *   - @Modifying chỉ dùng trong service (cần @Transactional)
 *   - Có thể thêm @QueryHint cho hiệu năng nếu cần
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    
}
