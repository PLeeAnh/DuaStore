package com.duastore.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class VNPAYService {

    private static final Logger log = LoggerFactory.getLogger(VNPAYService.class);

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    private static final String VERSION = "2.1.0";
    private static final String COMMAND = "pay";
    private static final String REFUND_COMMAND = "refund";
    private static final String ORDER_TYPE = "other";
    private static final String CURR_CODE = "VND";
    private static final String LOCALE = "vn";
    private static final String TRANSACTION_TYPE_REFUND = "02";

    public boolean isConfigured() {
        return tmnCode != null && !tmnCode.isBlank()
                && hashSecret != null && !hashSecret.isBlank();
    }

    public String createPaymentUrl(String txnRef, long amount, String orderInfo, HttpServletRequest req) {
        if (!isConfigured()) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", COMMAND);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", CURR_CODE);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", getIpAddress(req));
        params.put("vnp_CreateDate", formatDate(new Date()));
        params.put("vnp_ExpireDate", formatDate(addMinutes(new Date(), 15)));
        params.put("vnp_BankCode", "VNBANK");

        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(hashSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        return payUrl + "?" + buildQuery(params);
    }

    public Map<String, String> verifyReturn(Map<String, String> params) {
        Map<String, String> result = new HashMap<>();
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null) {
            result.put("success", "false");
            result.put("message", "Thiếu chữ ký");
            return result;
        }

        Map<String, String> verifyParams = new TreeMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        String hashData = buildHashData(verifyParams);
        String computedHash = hmacSHA512(hashSecret, hashData);

        if (!computedHash.equals(vnpSecureHash)) {
            result.put("success", "false");
            result.put("message", "Chữ ký không hợp lệ");
            return result;
        }

        result.put("success", "true");
        result.put("txnRef", params.get("vnp_TxnRef"));
        result.put("amount", params.get("vnp_Amount"));
        result.put("responseCode", params.get("vnp_ResponseCode"));
        result.put("transactionNo", params.get("vnp_TransactionNo"));
        result.put("bankCode", params.get("vnp_BankCode"));
        result.put("payDate", params.get("vnp_PayDate"));
        return result;
    }

    public Map<String, String> refundTransaction(
            String txnRef,
            long amount,
            String transactionNo,
            String transactionDate,
            String createdBy,
            String orderInfo,
            String ipAddr) {
        if (!isConfigured()) {
            Map<String, String> result = new HashMap<>();
            result.put("success", "false");
            result.put("message", "VNPAY chưa được cấu hình");
            return result;
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", VERSION);
        params.put("vnp_Command", REFUND_COMMAND);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_TransactionType", TRANSACTION_TYPE_REFUND);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_TransactionNo", transactionNo);
        params.put("vnp_TransactionDate", transactionDate);
        params.put("vnp_CreateBy", createdBy);
        params.put("vnp_CreateDate", formatDate(new Date()));
        params.put("vnp_IpAddr", ipAddr);
        params.put("vnp_OrderType", ORDER_TYPE);

        String hashData = buildHashData(params);
        String secureHash = hmacSHA512(hashSecret, hashData);
        params.put("vnp_SecureHash", secureHash);

        String query = buildQuery(params);
        String refundUrl = payUrl.replace("/vpcpay.html", "/vpcpay.html"); // VNPAY refund endpoint is same URL

        try {
            // Send POST request to VNPAY refund API
            java.net.URL url = new java.net.URL(refundUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = buildQuery(params).getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            StringBuilder response = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception e) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            Map<String, String> result = new HashMap<>();
            result.put("success", responseCode == 200 ? "true" : "false");
            result.put("responseCode", String.valueOf(responseCode));
            result.put("responseBody", response.toString());
            result.put("vnp_ResponseCode", extractResponseCode(response.toString()));
            result.put("vnp_TransactionNo", extractTransactionNo(response.toString()));
            return result;

        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("success", "false");
            result.put("message", "Lỗi gọi API VNPAY refund: " + e.getMessage());
            log.error("VNPAY refund error", e);
            return result;
        }
    }

    private String extractResponseCode(String response) {
        // Extract vnp_ResponseCode from response body
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("vnp_ResponseCode=([^&]+)");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractTransactionNo(String response) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("vnp_TransactionNo=([^&]+)");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String getIpAddress(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }

    private Date addMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private String buildHashData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append(key).append("=").append(value).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()))
                        .append("&");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xây dựng query", e);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(spec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tính HMAC-SHA512", e);
        }
    }
}
