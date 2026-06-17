package com.duastore.repository;

import com.duastore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByDanhMucIdAndIsActiveTrue(Integer danhMucId);
    List<Product> findByDanhMucIdInAndIsActiveTrue(List<Integer> danhMucIds);
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    List<Product> findByIsActiveTrueOrderByNgayTaoDesc();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN'")
    List<Product> findDangBan();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(String keyword);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) " +
           "AND (:trangThai IS NULL OR p.trangThaiSanPham = :trangThai) " +
           "ORDER BY p.ngayTao DESC")
    List<Product> searchWithFilters(@Param("keyword") String keyword,
                                    @Param("danhMucId") Integer danhMucId,
                                    @Param("trangThai") String trangThai);
}
