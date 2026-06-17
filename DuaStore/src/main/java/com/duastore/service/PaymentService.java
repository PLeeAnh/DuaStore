package com.duastore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class PaymentService {

    @Value("${payment.bank.code}")
    private String bankCode;

    @Value("${payment.bank.account.number}")
    private String accountNumber;

    @Value("${payment.bank.account.name}")
    private String accountName;

    public String getBankCode() {
        return bankCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String generateVietQrUrl(String maDon, long amount) {
        String des = encodeValue("Thanh toan don " + maDon);
        String holder = encodeValue(accountName);
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=TCB&amount=%d&des=%s&template=compact&holder=%s",
                accountNumber, amount, des, holder);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
