package com.duastore.repository;

import com.duastore.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT n FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findCustomerNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL")
    long countCustomerNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL AND n.id > :readMaxId")
    long countUnreadCustomerNotifications(Integer readMaxId);

    @Query("SELECT n FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF' ORDER BY n.createdAt DESC")
    List<Notification> findStaffNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF'")
    long countStaffNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF' AND n.id > :readMaxId")
    long countUnreadStaffNotifications(Integer readMaxId);
}
