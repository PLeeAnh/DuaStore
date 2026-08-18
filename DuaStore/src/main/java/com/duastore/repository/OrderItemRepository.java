package com.duastore.repository;

import com.duastore.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @EntityGraph(attributePaths = {"order"})
    List<OrderItem> findByOrderId(Integer orderId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.user.id = :userId")
    boolean existsByProductIdAndUserId(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.user.id = :userId AND (oi.order.trangThaiDon = 'DA_GIAO' OR oi.order.trangThaiDon = 'DA_HOAN_THANH')")
    boolean existsByProductIdAndUserIdAndPaid(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Query("SELECT oi.productId AS productId, SUM(oi.soLuong) AS total FROM OrderItem oi "
            + "WHERE (oi.order.trangThaiDon = 'DA_GIAO' OR oi.order.trangThaiDon = 'DA_HOAN_THANH') "
            + "GROUP BY oi.productId ORDER BY total DESC")
    List<Object[]> findTopSellingProductIds(Pageable pageable);

    @Query("SELECT COALESCE(SUM(oi.soLuong), 0) FROM OrderItem oi WHERE oi.productId = :productId AND (oi.order.trangThaiDon = 'DA_GIAO' OR oi.order.trangThaiDon = 'DA_HOAN_THANH')")
    long sumSoldQuantityByProductId(@Param("productId") Integer productId);

    @Query("SELECT oi.variantId, SUM(oi.soLuong) FROM OrderItem oi "
            + "WHERE oi.variantId IS NOT NULL "
            + "AND (oi.order.trangThaiDon = 'DA_GIAO' OR oi.order.trangThaiDon = 'DA_HOAN_THANH') "
            + "AND oi.order.ngayDat BETWEEN :start AND :end "
            + "GROUP BY oi.variantId")
    List<Object[]> sumSoldByVariantInRange(@Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("SELECT DISTINCT oi.productId FROM OrderItem oi WHERE oi.order.user.id = :userId "
            + "AND (oi.order.trangThaiDon = 'DA_GIAO' OR oi.order.trangThaiDon = 'DA_HOAN_THANH') "
            + "AND oi.order.ngayDat > :since")
    List<Integer> findPurchasedProductIdsByUserSince(@Param("userId") Integer userId,
            @Param("since") java.time.LocalDateTime since);

    @Query("SELECT DISTINCT oi2.productId FROM OrderItem oi1 "
            + "JOIN OrderItem oi2 ON oi1.order.id = oi2.order.id "
            + "WHERE oi1.productId = :productId "
            + "AND oi2.productId != :productId "
            + "AND (oi1.order.trangThaiDon = 'DA_GIAO' OR oi1.order.trangThaiDon = 'DA_HOAN_THANH')")
    List<Integer> findCoPurchasedProductIds(@Param("productId") Integer productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIdIn(@Param("orderIds") List<Integer> orderIds);
}
