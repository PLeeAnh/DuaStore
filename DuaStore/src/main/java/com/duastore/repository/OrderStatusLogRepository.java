package com.duastore.repository;

import com.duastore.model.OrderStatusLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Integer> {

    @EntityGraph(attributePaths = {"nguoiThucHien"})
    List<OrderStatusLog> findByOrderIdOrderByThoiGianAsc(Integer orderId);
}
