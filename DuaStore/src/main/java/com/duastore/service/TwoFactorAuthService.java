package com.duastore.service;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class TwoFactorAuthService {

    private static final int SECRET_BYTES = 20;
    private static final int DIGITS = 6;
    private static final int PERIOD = 30;
    private static final String CRYPTO = "HmacSHA1";

    private final UserRepository userRepository;

    public TwoFactorAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes)
                .replaceAll("=+$", "")
                .replaceAll("[^A-Za-z0-9]", "");
    }

    public String getQRCode(String secret, String email) {
        String uri = "otpauth://totp/"
                + urlEncode("DuaStore:" + email)
                + "?secret=" + secret
                + "&issuer=" + urlEncode("DuaStore")
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + PERIOD;
        return generateQRCodeSvg(uri);
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }

    private String generateQRCodeSvg(String data) {
        String encoded = Base64.getEncoder().encodeToString(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + encoded;
    }

    public boolean verify(String secret, int code) {
        long counter = Instant.now().getEpochSecond() / PERIOD;
        for (long i = -1; i <= 1; i++) {
            if (generateCode(secret, counter + i) == code) {
                return true;
            }
        }
        return false;
    }

    private int generateCode(String secret, long counter) {
        try {
            Mac mac = Mac.getInstance(CRYPTO);
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            mac.init(new SecretKeySpec(keyBytes, CRYPTO));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return binary % (int) Math.pow(10, DIGITS);
        } catch (Exception e) {
            return -1;
        }
    }

    @Transactional
    public void enableTwoFactor(Integer userId, String secret) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setTwoFactorSecret(secret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableTwoFactor(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setTwoFactorSecret(null);
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    public boolean isTwoFactorEnabled(Integer userId) {
        return userRepository.findById(userId)
                .map(User::getTwoFactorEnabled)
                .orElse(false);
    }
}
