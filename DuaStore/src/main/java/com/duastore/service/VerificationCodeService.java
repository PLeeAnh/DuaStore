package com.duastore.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private final ConcurrentHashMap<String, CodeEntry> codes = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private static final long TTL_MILLIS = 5 * 60 * 1000;
    private static final int MAX_ATTEMPTS = 5;

    public String generate(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        codes.put(email, new CodeEntry(code, System.currentTimeMillis() + TTL_MILLIS, 0));
        return code;
    }

    public boolean verify(String email, String code) {
        CodeEntry entry = codes.get(email);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expiry()) {
            codes.remove(email);
            return false;
        }
        if (!entry.code().equals(code)) {
            codes.computeIfPresent(email, (k, e) -> {
                CodeEntry next = e.recordFailedAttempt();
                return next.attempts() >= MAX_ATTEMPTS ? null : next;
            });
            return false;
        }
        return true;
    }

    public void delete(String email) {
        codes.remove(email);
    }

    private record CodeEntry(String code, long expiry, int attempts) {

        CodeEntry(String code, long expiry) {
            this(code, expiry, 0);
        }

        CodeEntry recordFailedAttempt() {
            return new CodeEntry(code, expiry, attempts + 1);
        }
    }
}
