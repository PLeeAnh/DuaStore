package com.duastore.service;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PaymentService {

    private final SiteSettingService siteSettingService;

    public PaymentService(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    public String getBankCode() {
        return siteSettingService.getValue("payment_bank_code", "MB");
    }

    public String getAccountNumber() {
        return siteSettingService.getValue("payment_bank_account", "118830072008");
    }

    public String getAccountName() {
        return siteSettingService.getValue("payment_bank_holder", "");
    }

    public String getBankName() {
        return siteSettingService.getValue("payment_bank_name", "MBBank");
    }

    public String getBankBranch() {
        return siteSettingService.getValue("payment_bank_branch", "");
    }

    public String getQrUrl() {
        return siteSettingService.getValue("payment_qr_url", "");
    }

    public Map<String, String> getBankInfo() {
        return Map.of(
            "bankName", getBankName(),
            "accountNumber", getAccountNumber(),
            "accountName", getAccountName(),
            "bankBranch", getBankBranch(),
            "qrUrl", getQrUrl()
        );
    }

    public String generateVietQrUrl(String maDon, long amount) {
        String bankCode = getBankCode();
        String accountNumber = getAccountNumber();
        String accountName = getAccountName();
        String des = encodeValue("Thanh toan don " + maDon);
        String holder = encodeValue(accountName);
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s&template=compact&holder=%s",
                accountNumber, bankCode, amount, des, holder);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
