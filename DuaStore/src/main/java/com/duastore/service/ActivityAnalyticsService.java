package com.duastore.service;

import com.duastore.model.UserActivityLog;
import com.duastore.repository.UserActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityAnalyticsService {

    private static final int ANALYSIS_DAYS = 30;
    private static final int MIN_ACTIVITIES_FOR_ANALYSIS = 5;
    private static final LocalTime DEFAULT_SEND_TIME = LocalTime.of(10, 0);
    private static final int PEAK_HOUR_RANGE = 2;

    private final UserActivityLogRepository activityLogRepository;

    public ActivityAnalyticsService(UserActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logActivity(Integer userId, String activityType, String description, HttpServletRequest request) {
        if (userId == null) return;

        UserActivityLog log = new UserActivityLog();
        log.setUserId(userId);
        log.setActivityType(activityType);
        log.setDescription(description);
        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            log.setIpAddress(ip);
        }
        activityLogRepository.save(log);
    }

    public LocalTime getOptimalSendTime(Integer userId) {
        if (userId == null) return DEFAULT_SEND_TIME;

        LocalDateTime since = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
        List<UserActivityLog> activities = activityLogRepository
                .findByUserIdAndActivityAtAfterOrderByActivityAtAsc(userId, since);

        if (activities.size() < MIN_ACTIVITIES_FOR_ANALYSIS) {
            return DEFAULT_SEND_TIME;
        }

        Map<Integer, Long> hourDistribution = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getActivityAt().getHour(),
                        Collectors.counting()
                ));

        int peakHour = hourDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DEFAULT_SEND_TIME.getHour());

        int adjustedHour = peakHour - PEAK_HOUR_RANGE;
        if (adjustedHour < 0) adjustedHour += 24;
        if (adjustedHour < 6) adjustedHour = 8;

        return LocalTime.of(adjustedHour, 0);
    }

    public Map<String, Long> getHourDistribution(Integer userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(ANALYSIS_DAYS);
        List<UserActivityLog> activities = activityLogRepository
                .findByUserIdAndActivityAtAfterOrderByActivityAtAsc(userId, since);

        return activities.stream()
                .collect(Collectors.groupingBy(
                        a -> String.format("%02d:00", a.getActivityAt().getHour()),
                        Collectors.counting()
                ));
    }
}
