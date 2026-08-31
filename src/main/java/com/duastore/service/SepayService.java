package com.duastore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SepayService {

    private static final Logger log = LoggerFactory.getLogger(SepayService.class);
    private final SiteSettingService siteSettingService;

    public SepayService(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    public String getMerchantId() {
        return siteSettingService.getValue("sepay_merchant_id");
    }

    public String getSecretKey() {
        return siteSettingService.getValue("sepay_secret_key");
    }

    public boolean isConfigured() {
        String mid = getMerchantId();
        String sk = getSecretKey();
        return mid != null && !mid.isBlank() && sk != null && !sk.isBlank();
    }

    public String getBankName() {
        return siteSettingService.getValue("payment_bank_name");
    }

    public String getBankAccount() {
        return siteSettingService.getValue("payment_bank_account");
    }

    public String getBankHolder() {
        return siteSettingService.getValue("payment_bank_holder");
    }

    public String generateQrUrl(long amount, String description) {
        String account = getBankAccount();
        String bank = getBankName();
        if (account == null || bank == null) {
            return null;
        }
        String encodedDes = URLEncoder.encode(description, StandardCharsets.UTF_8);
        return "https://vietqr.app/img?acc=" + account
                + "&bank=" + URLEncoder.encode(bank, StandardCharsets.UTF_8)
                + "&amount=" + amount
                + "&des=" + encodedDes;
    }

    public boolean verifyApiKey(String authorizationHeader) {
        String secretKey = getSecretKey();
        if (secretKey == null || secretKey.isBlank()) {
            return false;
        }
        if (authorizationHeader == null || !authorizationHeader.startsWith("Apikey ")) {
            return false;
        }
        String providedKey = authorizationHeader.substring(7).trim();
        return java.security.MessageDigest.isEqual(
                providedKey.getBytes(StandardCharsets.UTF_8),
                secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
