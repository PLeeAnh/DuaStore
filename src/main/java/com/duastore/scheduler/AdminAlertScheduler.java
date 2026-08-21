package com.duastore.scheduler;

import com.duastore.service.admin.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/**
 * phía quản trị (admin) — Tác vụ chạy định kỳ (scheduled job) xử lý cảnh báo hệ thống.
 */
public class AdminAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AdminAlertScheduler.class);

    private final AlertService alertService;

    public AdminAlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void checkLowStock() {
        log.info("Checking low stock...");
        alertService.checkLowStock(20);
    }

    @Scheduled(cron = "0 30 7 * * *")
    public void checkUrgentOrders() {
        log.info("Checking urgent orders...");
        alertService.checkUrgentOrders(48);
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendAbandonedCartReminders() {
        log.info("Sending abandoned cart reminders...");
        alertService.sendAbandonedCartReminders(24);
    }
}
