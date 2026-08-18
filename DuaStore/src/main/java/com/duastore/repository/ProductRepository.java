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

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.danhMucId IN :danhMucIds AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true)")
    List<Product> findByDanhMucIdInAndIsActiveTrue(@Param("danhMucIds") List<Integer> danhMucIds);

    @Query("SELECT p FROM Product p WHERE p.isFeatured = true AND p.isActive = true AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true)")
    List<Product> findFeaturedWithVariants();

    List<Product> findByIsActiveTrueOrderByNgayTaoDesc();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN'")
    List<Product> findDangBan();

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.ngayTao DESC")
    List<Product> searchByName(String keyword);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' AND (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.ngayTao DESC")
    List<Product> findTopByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY p.ngayTao DESC")
    Page<Product> searchByNamePaged(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) ORDER BY p.ngayTao DESC")
    Page<Product> findDangBanPaged(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.danhMucId IN :danhMucIds AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true)")
    Page<Product> findByDanhMucIdInAndIsActiveTrue(@Param("danhMucIds") List<Integer> danhMucIds, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:trangThai IS NULL OR p.trangThaiSanPham = :trangThai) "
            + "ORDER BY p.ngayTao DESC")
    List<Product> searchWithFilters(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("trangThai") String trangThai);

    @Query("SELECT p FROM Product p WHERE p.isActive = true "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:trangThai IS NULL OR p.trangThaiSanPham = :trangThai) "
            + "ORDER BY p.ngayTao DESC")
    Page<Product> searchWithFiltersPaged(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("trangThai") String trangThai,
            Pageable pageable);

    Page<Product> findByIsActiveTrueOrderByNgayTaoDesc(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) ORDER BY p.ngayTao DESC")
    Page<Product> findNewestWithVariants(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:minPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) >= :minPrice)) "
            + "AND (:maxPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) <= :maxPrice)) "
            + "AND (:dungTich IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND v.dungTich = :dungTich)) "
            + "AND (:chatLieu IS NULL OR p.chatLieu = :chatLieu) ")
    Page<Product> filterPaged(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("dungTich") Integer dungTich,
            @Param("chatLieu") String chatLieu,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:chatLieu IS NULL OR p.chatLieu = :chatLieu) "
            + "AND (:minPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) >= :minPrice)) "
            + "AND (:maxPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) <= :maxPrice)) "
            + "AND (:dungTich IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND v.dungTich = :dungTich)) "
            + "ORDER BY (SELECT MIN(COALESCE(v2.giaKhuyenMai, v2.giaGoc)) FROM ProductVariant v2 WHERE v2.productId = p.id AND v2.isActive = true) ASC")
    Page<Product> filterPagedPriceAsc(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("dungTich") Integer dungTich,
            @Param("chatLieu") String chatLieu,
            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:chatLieu IS NULL OR p.chatLieu = :chatLieu) "
            + "AND (:minPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) >= :minPrice)) "
            + "AND (:maxPrice IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) <= :maxPrice)) "
            + "AND (:dungTich IS NULL OR EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND v.dungTich = :dungTich)) "
            + "ORDER BY (SELECT MIN(COALESCE(v2.giaKhuyenMai, v2.giaGoc)) FROM ProductVariant v2 WHERE v2.productId = p.id AND v2.isActive = true) DESC")
    Page<Product> filterPagedPriceDesc(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("dungTich") Integer dungTich,
            @Param("chatLieu") String chatLieu,
            Pageable pageable);

    @Query(value = "SELECT p.id FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:chatLieu IS NULL OR p.chatLieu = :chatLieu) "
            + "ORDER BY (SELECT COALESCE(AVG(r.danhGia), 0) FROM Review r WHERE r.productId = p.id) DESC",
            countQuery = "SELECT COUNT(DISTINCT p.id) FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham = 'DANG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true) "
            + "AND (:keyword IS NULL OR (LOWER(p.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.chatLieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.thuongHieu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.xuatXu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.mucDichSuDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.kinhLoai) LIKE LOWER(CONCAT('%', :keyword, '%')))) "
            + "AND (:danhMucId IS NULL OR p.danhMucId = :danhMucId) "
            + "AND (:chatLieu IS NULL OR p.chatLieu = :chatLieu)")
    Page<Integer> findIdsFilteredTopRated(@Param("keyword") String keyword,
            @Param("danhMucId") Integer danhMucId,
            @Param("chatLieu") String chatLieu,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.isActive = true AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true)")
    List<Product> findAllByIdWithVariants(@Param("ids") List<Integer> ids);

    @Query("SELECT DISTINCT p.chatLieu FROM Product p WHERE p.isActive = true AND p.chatLieu IS NOT NULL ORDER BY p.chatLieu ASC")
    List<String> findDistinctChatLieu();

    @Query("SELECT DISTINCT p.thuongHieu FROM Product p WHERE p.isActive = true AND p.thuongHieu IS NOT NULL ORDER BY p.thuongHieu ASC")
    List<String> findDistinctThuongHieu();

    @Query("SELECT DISTINCT p.xuatXu FROM Product p WHERE p.isActive = true AND p.xuatXu IS NOT NULL ORDER BY p.xuatXu ASC")
    List<String> findDistinctXuatXu();

    @Query("SELECT DISTINCT p.kinhLoai FROM Product p WHERE p.isActive = true AND p.kinhLoai IS NOT NULL ORDER BY p.kinhLoai ASC")
    List<String> findDistinctKinhLoai();

    @Query("SELECT DISTINCT p.mucDichSuDung FROM Product p WHERE p.isActive = true AND p.mucDichSuDung IS NOT NULL ORDER BY p.mucDichSuDung ASC")
    List<String> findDistinctMucDichSuDung();

    @Query("SELECT p.thuongHieu, COUNT(p) FROM Product p WHERE p.isActive = true AND p.thuongHieu IS NOT NULL "
            + "GROUP BY p.thuongHieu ORDER BY COUNT(p) DESC, p.thuongHieu ASC")
    List<Object[]> findThuongHieuCounts();

    @Query("SELECT p.chatLieu, COUNT(p) FROM Product p WHERE p.isActive = true AND p.chatLieu IS NOT NULL "
            + "GROUP BY p.chatLieu ORDER BY COUNT(p) DESC, p.chatLieu ASC")
    List<Object[]> findChatLieuCounts();

    @Query("SELECT p.xuatXu, COUNT(p) FROM Product p WHERE p.isActive = true AND p.xuatXu IS NOT NULL "
            + "GROUP BY p.xuatXu ORDER BY COUNT(p) DESC, p.xuatXu ASC")
    List<Object[]> findXuatXuCounts();

    @Query("SELECT p.kinhLoai, COUNT(p) FROM Product p WHERE p.isActive = true AND p.kinhLoai IS NOT NULL "
            + "GROUP BY p.kinhLoai ORDER BY COUNT(p) DESC, p.kinhLoai ASC")
    List<Object[]> findKinhLoaiCounts();

    @Query("SELECT p.mucDichSuDung, COUNT(p) FROM Product p WHERE p.isActive = true AND p.mucDichSuDung IS NOT NULL "
            + "GROUP BY p.mucDichSuDung ORDER BY COUNT(p) DESC, p.mucDichSuDung ASC")
    List<Object[]> findMucDichSuDungCounts();

    @Query("SELECT p.hinhDang, COUNT(p) FROM Product p WHERE p.isActive = true AND p.hinhDang IS NOT NULL "
            + "GROUP BY p.hinhDang ORDER BY COUNT(p) DESC, p.hinhDang ASC")
    List<Object[]> findHinhDangCounts();

    long countByDanhMucIdAndIsActiveTrue(Integer danhMucId);

    @Query("SELECT p.danhMucId, COUNT(p) FROM Product p WHERE p.isActive = true GROUP BY p.danhMucId")
    List<Object[]> countProductsByDanhMuc();

    @Query("SELECT DISTINCT p FROM Product p WHERE p.isActive = true AND p.trangThaiSanPham != 'NGUNG_BAN' "
            + "AND EXISTS (SELECT 1 FROM ProductVariant v WHERE v.productId = p.id AND v.isActive = true AND COALESCE(v.giaKhuyenMai, v.giaGoc) <= :maxPrice) "
            + "ORDER BY p.ngayTao DESC")
    List<Product> findUnderPrice(@Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
}
