package com.duastore.repository;

import com.duastore.model.OrderNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu đơn hàng.
 */
public interface OrderNoteRepository extends JpaRepository<OrderNote, Integer> {

    @Query("SELECT n FROM OrderNote n JOIN FETCH n.admin WHERE n.order.id = ?1 ORDER BY n.ngayTao ASC")
    List<OrderNote> findByOrderIdOrderByNgayTaoAsc(Integer orderId);

    void deleteByOrderId(Integer orderId);
}
