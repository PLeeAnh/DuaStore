package com.duastore.repository;

import com.duastore.model.PurchaseOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    Optional<PurchaseOrder> findByMaPhieu(String maPhieu);
    List<PurchaseOrder> findByTrangThaiOrderByCreatedAtDesc(String trangThai, Pageable pageable);
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p.trangThai, COUNT(p) FROM PurchaseOrder p GROUP BY p.trangThai")
    List<Object[]> countGroupByTrangThai();

    @Query("SELECT COALESCE(SUM(p.tongTien), 0) FROM PurchaseOrder p WHERE p.trangThai IN ('HOAN_THANH', 'DA_DUYET', 'DANG_NHAP') AND p.ngayNhap BETWEEN :start AND :end")
    BigDecimal sumTongTienByNgayNhapBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByTrangThai(String trangThai);
}
