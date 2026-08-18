package com.duastore.scheduler;

import com.duastore.service.LoyaltyPointsService;
import com.duastore.service.SiteSettingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyPointsExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(LoyaltyPointsExpiryScheduler.class);

    private final LoyaltyPointsService loyaltyPointsService;
    private final SiteSettingService siteSettingService;

    public LoyaltyPointsExpiryScheduler(LoyaltyPointsService loyaltyPointsService,
            SiteSettingService siteSettingService) {
        this.loyaltyPointsService = loyaltyPointsService;
        this.siteSettingService = siteSettingService;
    }

    @Scheduled(cron = "0 0 3 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void expireOldLoyaltyPoints() {
        if (!loyaltyPointsService.isPointsExpiryEnabled()) {
            log.info("Loyalty points expiry is disabled, skipping");
            return;
        }

        try {
            loyaltyPointsService.expireOldPoints();
            log.info("Loyalty points expiry job completed");
        } catch (Exception e) {
            log.error("Error running loyalty points expiry job", e);
        }
    }
}