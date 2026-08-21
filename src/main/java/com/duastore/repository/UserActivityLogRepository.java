package com.duastore.repository;

import com.duastore.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
/**
 * Repository (Spring Data JPA) truy vấn/thao tác dữ liệu người dùng, nhật ký hoạt động người dùng, nhật ký hệ thống.
 */
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByUserIdOrderByActivityAtDesc(Integer userId);

    List<UserActivityLog> findByUserIdAndActivityAtAfterOrderByActivityAtAsc(Integer userId, LocalDateTime since);

    long countByUserIdAndActivityTypeAndActivityAtAfter(Integer userId, String activityType, LocalDateTime since);

    @Query("SELECT DATEPART(HOUR, ual.activityAt), COUNT(ual) " +
            "FROM UserActivityLog ual WHERE ual.userId = :userId " +
            "AND ual.activityAt > :since GROUP BY DATEPART(HOUR, ual.activityAt) " +
            "ORDER BY COUNT(ual) DESC")
    List<Object[]> findHourlyActivityDistribution(@Param("userId") Integer userId, @Param("since") LocalDateTime since);
}
