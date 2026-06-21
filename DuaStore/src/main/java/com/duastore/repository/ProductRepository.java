package com.duastore.repository;

import com.duastore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    long countByIsActiveTrue();
    long countByIsFeaturedTrueAndIsActiveTrue();

    List<Product> findByDanhMucIdAndIsActiveTrue(Integer danhMucId);
    List<Product> findByDanhMucIdInAndIsActiveTrue(List<Integer> danhMucIds);
    List<Product> findByIsFeaturedTrueAndIsActiveTrue();
    List<Product> findByIsActiveTrueOrderByNgayTaoDesc();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN'")
    List<Product> findDangBan();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.ngayTao DESC")
    List<Product> searchByName(String keyword);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.ngayTao DESC")
    Page<Product> searchByNamePaged(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' ORDER BY p.ngayTao DESC")
    Page<Product> findDangBanPaged(Pageable pageable);

    Page<Product> findByDanhMucIdInAndIsActiveTrue(List<Integer> danhMucIds, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) " +
           "AND (:trangThai IS NULL OR p.trangThaiSanPham = :trangThai) " +
           "ORDER BY p.ngayTao DESC")
    List<Product> searchWithFilters(@Param("keyword") String keyword,
                                    @Param("danhMucId") Integer danhMucId,
                                    @Param("trangThai") String trangThai);

    @Query("SELECT p FROM Product p WHERE p.isActive = true " +
           "AND (:keyword IS NULL OR LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) " +
           "AND (:trangThai IS NULL OR p.trangThaiSanPham = :trangThai) " +
           "ORDER BY p.ngayTao DESC")
    Page<Product> searchWithFiltersPaged(@Param("keyword") String keyword,
                                         @Param("danhMucId") Integer danhMucId,
                                         @Param("trangThai") String trangThai,
                                         Pageable pageable);

    Page<Product> findByIsActiveTrueOrderByNgayTaoDesc(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' " +
           "AND (:keyword IS NULL OR LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) " +
           "AND (:minPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) >= :minPrice)) " +
           "AND (:maxPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) <= :maxPrice)) " +
           "AND (:dungTich IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND v.dungTich = :dungTich)) " +
           "AND (:kieuNap IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND LOWER(v.tenBienThe) LIKE LOWER(CONCAT('%', :kieuNap, '%')))) " +
           "AND (:hinhDang IS NULL OR p.hinhDang = :hinhDang) ")
    Page<Product> filterPaged(@Param("keyword") String keyword,
                              @Param("danhMucId") Integer danhMucId,
                              @Param("minPrice") BigDecimal minPrice,
                              @Param("maxPrice") BigDecimal maxPrice,
                              @Param("dungTich") Integer dungTich,
                              @Param("kieuNap") String kieuNap,
                              @Param("hinhDang") String hinhDang,
                              Pageable pageable);

    @Query("SELECT DISTINCT p.hinhDang FROM Product p WHERE p.isActive = true AND p.hinhDang IS NOT NULL ORDER BY p.hinhDang ASC")
    List<String> findDistinctHinhDang();
}
