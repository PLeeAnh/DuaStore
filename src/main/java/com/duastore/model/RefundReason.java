package com.duastore.model;

/**
 * Enum liệt kê các giá trị hợp lệ cho hoàn trả/đổi trả đơn hàng.
 */
public enum RefundReason {
    LOI_HANG("Lỗi hàng (vỡ, nứt, khác mô tả)"),
    KHONG_DUNG_MO_TA("Không đúng mô tả/hình ảnh"),
    DOI_Y("Đổi ý (không thích, mua nhầm)"),
    KHAC("Lý do khác");

    private final String displayName;

    RefundReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RefundReason fromCode(String code) {
        for (RefundReason reason : values()) {
            if (reason.name().equals(code)) {
                return reason;
            }
        }
        return KHAC;
    }

    public boolean isShopFault() {
        return this == LOI_HANG || this == KHONG_DUNG_MO_TA;
    }

    public boolean requiresVideoProof() {
        return this == LOI_HANG || this == KHONG_DUNG_MO_TA;
    }
}