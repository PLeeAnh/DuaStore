package com.duastore.repository;

import com.duastore.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.user.id = :userId")
    boolean existsByProductIdAndUserId(@Param("productId") Integer productId, @Param("userId") Integer userId);

    @Query("SELECT CASE WHEN COUNT(oi) > 0 THEN true ELSE false END FROM OrderItem oi WHERE oi.productId = :productId AND oi.order.user.id = :userId AND oi.order.trangThaiTT = 'DA_THANH_TOAN'")
    boolean existsByProductIdAndUserIdAndPaid(@Param("productId") Integer productId, @Param("userId") Integer userId);
}
