package com.duastore.repository;

import com.duastore.model.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Integer> {

    @Query("SELECT l FROM OrderStatusLog l LEFT JOIN FETCH l.nguoiThucHien WHERE l.order.id = ?1 ORDER BY l.thoiGian ASC")
    List<OrderStatusLog> findByOrderIdOrderByThoiGianAsc(Integer orderId);

    void deleteByOrderId(Integer orderId);
}
