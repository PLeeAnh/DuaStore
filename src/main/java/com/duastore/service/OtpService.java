package com.duastore.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý mã OTP.
 */
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    /**
     * Tạo OTP và lưu vào bộ nhớ
     */
    public String generateAndStore(String email, String purpose) {

        String otp = String.format(
                "%06d",
                RANDOM.nextInt(900000) + 100000
        );

        store.put(
                buildKey(email, purpose),
                new OtpEntry(
                        otp,
                        LocalDateTime.now(),
                        new AtomicInteger(0)
                )
        );

        return otp;
    }

    /**
     * Xác thực OTP
     */
    public boolean verify(
            String email,
            String purpose,
            String inputOtp
    ) {

        if (inputOtp == null || inputOtp.isBlank()) {
            return false;
        }

        String key = buildKey(email, purpose);

        OtpEntry entry = store.get(key);

        if (entry == null) {
            return false;
        }

        // Hết hạn
        if (entry.isExpired()) {
            store.remove(key);
            return false;
        }

        // Đúng OTP
        if (entry.otp.equals(inputOtp.trim())) {
            store.remove(key);
            return true;
        }

        // Sai OTP
        int attempts = entry.attempts.incrementAndGet();

        // Quá số lần cho phép
        if (attempts >= MAX_ATTEMPTS) {
            store.remove(key);
        }

        return false;
    }

    /**
     * Xóa OTP thủ công
     */
    public void invalidate(String email, String purpose) {
        store.remove(buildKey(email, purpose));
    }

    /**
     * Dọn OTP hết hạn mỗi phút
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredOtps() {
        store.entrySet().removeIf(
                entry -> entry.getValue().isExpired()
        );
    }

    /**
     * Tạo key duy nhất
     */
    private String buildKey(String email, String purpose) {

        if (email == null || purpose == null) {
            throw new IllegalArgumentException(
                    "Email và purpose không được null"
            );
        }

        return email.trim().toLowerCase()
                + ":"
                + purpose.trim().toUpperCase();
    }

    /**
     * Thông tin OTP
     */
    private static class OtpEntry {

        private final String otp;
        private final LocalDateTime createdAt;
        private final AtomicInteger attempts;

        public OtpEntry(
                String otp,
                LocalDateTime createdAt,
                AtomicInteger attempts
        ) {
            this.otp = otp;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }

        public boolean isExpired() {
            return createdAt
                    .plusMinutes(OTP_EXPIRY_MINUTES)
                    .isBefore(LocalDateTime.now());
        }
    }

}
