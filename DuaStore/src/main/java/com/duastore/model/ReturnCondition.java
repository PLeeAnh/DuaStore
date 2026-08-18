package com.duastore.model;

public enum ReturnCondition {
    NGUYEN_VINH("Nguyên vẹn (đủ hộp, phụ kiện, tem mác)"),
    VO_VANG("Vỡ/Vết nứt (do khách đóng gói không cẩn thận)"),
    THIEU_PHU_KIEN("Thiếu phụ kiện/hộp/tem mác"),
    CHUA_NHAN("Chưa nhận hàng trả về kho");

    private final String displayName;

    ReturnCondition(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ReturnCondition fromCode(String code) {
        for (ReturnCondition condition : values()) {
            if (condition.name().equals(code)) {
                return condition;
            }
        }
        return CHUA_NHAN;
    }

    public boolean isAcceptableForFullRefund() {
        return this == NGUYEN_VINH;
    }

    public boolean isAcceptableForPartialRefund() {
        return this == THIEU_PHU_KIEN;
    }

    public boolean isRejected() {
        return this == VO_VANG;
    }
}