package com.duastore.repository;

import com.duastore.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    Page<Order> findByUserId(Integer userId, Pageable pageable);
    Page<Order> findByUserIdAndTrangThaiDon(Integer userId, String trangThaiDon, Pageable pageable);
    Page<Order> findAllBy(Pageable pageable);
}
