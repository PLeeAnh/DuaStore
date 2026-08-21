package com.duastore.scheduler;

import com.duastore.service.admin.BirthdayVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/**
 * Tác vụ chạy định kỳ (scheduled job) xử lý voucher sinh nhật, voucher/mã giảm giá.
 */
public class BirthdayVoucherScheduler {

    private static final Logger log = LoggerFactory.getLogger(BirthdayVoucherScheduler.class);

    private final BirthdayVoucherService birthdayVoucherService;

    public BirthdayVoucherScheduler(BirthdayVoucherService birthdayVoucherService) {
        this.birthdayVoucherService = birthdayVoucherService;
    }

    @Scheduled(cron = "0 0 6 * * *")
    public void generateBirthdayVouchers() {
        int count = birthdayVoucherService.generateBirthdayVouchers();
        if (count > 0) {
            log.info("Generated {} birthday voucher(s)", count);
        }
    }
}
