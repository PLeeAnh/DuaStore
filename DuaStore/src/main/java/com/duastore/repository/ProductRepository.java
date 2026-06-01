/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.repository;

import com.duastore.model.Product;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * ★ ProductRepository — Repository cho Product entity
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Repository interface cho entity Product.
 * Kế thừa JpaRepository để có sẵn các phương thức CRUD + phân trang.
 * 
 * Spring Data JPA sẽ tự động implement các method dựa trên tên phương thức.
 * Sử dụng @Query cho các truy vấn phức tạp.
 * 
 * ★ TODO [Nguyễn Thị E]: Phương thức tìm kiếm cơ bản
 *   - List<Product> findByCategoryId(Long categoryId)
 *   - List<Product> findByStatus(Integer status)
 *   - List<Product> findByCategoryIdAndStatus(Long categoryId, Integer status)
 * 
 * ★ TODO [Nguyễn Thị E]: Tìm kiếm theo tên (search)
 *   - @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword%")
 *     Page<Product> searchByName(@Param("keyword") String keyword, Pageable pageable)
 *   - Hoặc dùng Specification / QueryDSL cho tìm kiếm nâng cao
 * 
 * ★ TODO [Nguyễn Thị E]: Sản phẩm bán chạy
 *   - @Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY ...")
 *     List<Product> findTopSelling(Pageable pageable)
 *   - Cần join với bảng order_details để tính số lượng đã bán
 * 
 * ★ TODO [Nguyễn Thị E]: Sản phẩm mới nhất
 *   - List<Product> findTopByOrderByCreatedAtDesc(Pageable pageable)
 *   - Hoặc @Query với điều kiện status = 1
 * 
 * ★ TODO [Nguyễn Thị E]: Phân trang + lọc
 *   - Page<Product> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable)
 *   - Page<Product> findAllByStatus(Integer status, Pageable pageable)
 * 
 * ⚠ Lưu ý:
 *   - Tận dụng tên method của Spring Data JPA để giảm @Query
 *   - Sử dụng Pageable cho phân trang thay vì List
 *   - Luôn thêm điều kiện status = 1 (active) cho client-facing queries
 *   - @Repository là optional (Spring Data JPA tự nhận diện)
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
}
