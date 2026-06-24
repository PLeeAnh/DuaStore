package com.duastore.repository;

import com.duastore.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Notification> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Notification> findTop5ByIsActiveTrueOrderByCreatedAtDesc();
    long countByIsActiveTrue();
    long countByIsActiveTrueAndIdGreaterThan(Integer id);
    Optional<Notification> findTopByIsActiveTrueOrderByIdDesc();
}
