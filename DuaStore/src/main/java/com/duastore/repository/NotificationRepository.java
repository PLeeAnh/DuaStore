package com.duastore.repository;

import com.duastore.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT n FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL AND (n.userId IS NULL OR n.userId = :userId) ORDER BY n.createdAt DESC")
    List<Notification> findCustomerNotifications(@Param("userId") Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL AND (n.userId IS NULL OR n.userId = :userId)")
    long countCustomerNotifications(@Param("userId") Integer userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL AND (n.userId IS NULL OR n.userId = :userId) AND n.id > :readMaxId")
    long countUnreadCustomerNotifications(@Param("userId") Integer userId, @Param("readMaxId") Integer readMaxId);

    @Query("SELECT n FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF' ORDER BY n.createdAt DESC")
    List<Notification> findStaffNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF'")
    long countStaffNotifications();

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.isActive = true AND n.targetRole = 'STAFF' AND n.id > :readMaxId")
    long countUnreadStaffNotifications(Integer readMaxId);

    @Query("SELECT n FROM Notification n WHERE n.isActive = true AND n.targetRole IS NULL AND (n.userId IS NULL OR n.userId = :userId) AND n.id > :readMaxId ORDER BY n.createdAt DESC")
    List<Notification> findNewByUserId(@Param("userId") Integer userId, @Param("readMaxId") Integer readMaxId);
}
