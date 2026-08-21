package com.duastore.util;

/**
 * Lớp tiện ích (utility) hỗ trợ xử lý thanh toán.
 */
public final class PaymentStatusUtil {

    private static final String DA_THANH_TOAN = "DA_THANH_TOAN";
    private static final String CHUA_THANH_TOAN = "CHUA_THANH_TOAN";

    public static String getDisplayName(String code) {
        if (DA_THANH_TOAN.equals(code)) {
            return "Đã thanh toán";
        }
        if (CHUA_THANH_TOAN.equals(code)) {
            return "Chưa thanh toán";
        }
        return code;
    }

    public static String getBadgeClass(String code) {
        if (DA_THANH_TOAN.equals(code)) {
            return "bg-success";
        }
        if (CHUA_THANH_TOAN.equals(code)) {
            return "bg-warning text-dark";
        }
        return "bg-secondary";
    }

    public static String getPaymentIcon(String phuongThuc) {
        if ("COD".equals(phuongThuc)) {
            return "bi-cash-stack";
        }
        if ("CHUYEN_KHOAN".equals(phuongThuc)) {
            return "bi-bank";
        }
        if ("VNPAY".equals(phuongThuc)) {
            return "bi-credit-card-2-front";
        }
        return "bi-credit-card";
    }

    public static String getPaymentMethodLabel(String phuongThuc) {
        if ("COD".equals(phuongThuc)) {
            return "COD";
        }
        if ("CHUYEN_KHOAN".equals(phuongThuc)) {
            return "Chuyển khoản";
        }
        if ("VNPAY".equals(phuongThuc)) {
            return "VNPay";
        }
        return phuongThuc;
    }

    private PaymentStatusUtil() {
    }
}
