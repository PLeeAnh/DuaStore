package com.duastore.repository;

import com.duastore.model.OrderNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderNoteRepository extends JpaRepository<OrderNote, Integer> {

    @EntityGraph(attributePaths = {"admin"})
    List<OrderNote> findByOrderIdOrderByNgayTaoAsc(Integer orderId);
}
