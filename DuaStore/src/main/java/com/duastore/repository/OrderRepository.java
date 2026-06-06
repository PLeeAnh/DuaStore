package com.duastore.repository;

import com.duastore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUserId(Integer userId, Pageable pageable);
    Page<Order> findByUserIdAndTrangThaiDon(Integer userId, String trangThaiDon, Pageable pageable);
    Page<Order> findAllBy(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.ngayDat BETWEEN :start AND :end")
    long countByNgayDatBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.tongThanhToan), 0) FROM Order o WHERE o.trangThaiDon = 'DA_GIAO' AND o.ngayDat BETWEEN :start AND :end")
    BigDecimal sumTongThanhToanByTrangThaiDonAndNgayDatBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o ORDER BY o.ngayDat DESC")
    List<Order> findTop10ByOrderByNgayDatDesc(Pageable pageable);

    @Query("SELECT o.trangThaiDon, COUNT(o) FROM Order o GROUP BY o.trangThaiDon")
    List<Object[]> countGroupByTrangThaiDon();

    long countByTrangThaiDon(String trangThaiDon);
}
