package com.duastore.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    public String generateAndStore(String email, String purpose) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        store.put(buildKey(email, purpose),
                  new OtpEntry(otp, LocalDateTime.now(), 0));
        return otp;
    }

    public boolean verify(String email, String purpose, String inputOtp) {
        String key = buildKey(email, purpose);
        OtpEntry entry = store.get(key);
        if (entry == null) return false;
        if (entry.isExpired()) { store.remove(key); return false; }
        if (entry.attempts >= MAX_ATTEMPTS) return false;

        entry.attempts++;
        if (entry.otp.equals(inputOtp.trim())) {
            store.remove(key);
            return true;
        }
        return false;
    }

    public void invalidate(String email, String purpose) {
        store.remove(buildKey(email, purpose));
    }

    private String buildKey(String email, String purpose) {
        return email.toLowerCase() + ":" + purpose;
    }

    private static class OtpEntry {
        String otp;
        LocalDateTime createdAt;
        int attempts;

        OtpEntry(String otp, LocalDateTime createdAt, int attempts) {
            this.otp = otp;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }

        boolean isExpired() {
            return createdAt.plusMinutes(OTP_EXPIRY_MINUTES)
                            .isBefore(LocalDateTime.now());
        }
    }
}
