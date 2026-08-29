package com.duastore.repository;

import com.duastore.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn lịch sử xuất nhập kho biến thể sản phẩm.
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Integer> {

    /** Lấy lịch sử theo biến thể, phân trang, mới nhất lên đầu */
    Page<StockMovement> findByVariantIdOrderByCreatedAtDesc(Integer variantId, Pageable pageable);

    /** Lấy toàn bộ lịch sử theo biến thể (không phân trang) */
    List<StockMovement> findByVariantIdOrderByCreatedAtDesc(Integer variantId);

    /** Tổng nhập theo biến thể trong khoảng thời gian */
    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.variantId = :variantId AND sm.type = 'IN' AND sm.createdAt BETWEEN :start AND :end")
    long sumInByVariantAndDateRange(@Param("variantId") Integer variantId,
                                     @Param("start") java.time.LocalDateTime start,
                                     @Param("end") java.time.LocalDateTime end);

    /** Tổng xuất theo biến thể trong khoảng thời gian */
    @Query("SELECT COALESCE(SUM(ABS(sm.quantity)), 0) FROM StockMovement sm WHERE sm.variantId = :variantId AND sm.type = 'OUT' AND sm.createdAt BETWEEN :start AND :end")
    long sumOutByVariantAndDateRange(@Param("variantId") Integer variantId,
                                      @Param("start") java.time.LocalDateTime start,
                                      @Param("end") java.time.LocalDateTime end);

    /** Tìm theo đơn hàng */
    List<StockMovement> findByOrderIdOrderByCreatedAtDesc(Integer orderId);

    /** Tìm theo người thực hiện */
    Page<StockMovement> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);
}
