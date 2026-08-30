package com.duastore.repository;

import com.duastore.model.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PageViewRepository extends JpaRepository<PageView, Integer> {
    
    @Query("SELECT p.eventType, COUNT(DISTINCT p.sessionId) FROM PageView p WHERE p.createdAt BETWEEN :start AND :end GROUP BY p.eventType")
    List<Object[]> countUniqueSessionsByEventType(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT p.sessionId) FROM PageView p WHERE p.eventType = 'PAGE_VIEW' AND p.createdAt BETWEEN :start AND :end")
    long countUniqueVisitors(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.sessionId) FROM PageView p WHERE p.eventType = 'ADD_TO_CART' AND p.createdAt BETWEEN :start AND :end")
    long countAddToCartSessions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT p.sessionId) FROM PageView p WHERE p.eventType = 'CHECKOUT_START' AND p.createdAt BETWEEN :start AND :end")
    long countCheckoutSessions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT p.sessionId) FROM PageView p WHERE p.eventType = 'ORDER_COMPLETE' AND p.createdAt BETWEEN :start AND :end")
    long countPaidSessions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.pagePath, COUNT(p.id) as cnt FROM PageView p WHERE p.eventType = 'PAGE_VIEW' AND p.createdAt BETWEEN :start AND :end GROUP BY p.pagePath ORDER BY cnt DESC")
    List<Object[]> findTopPages(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, org.springframework.data.domain.Pageable pageable);
    
    List<PageView> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
