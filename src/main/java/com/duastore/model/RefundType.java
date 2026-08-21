package com.duastore.model;

/**
 * Enum liệt kê các giá trị hợp lệ cho hoàn trả/đổi trả đơn hàng.
 */
public enum RefundType {
    HOAN_TIEN("Hoàn tiền"),
    DOI_SIZE("Đổi size"),
    DOI_MAU("Đổi màu"),
    DOI_SAN_PHAM_KHAC("Đổi sản phẩm khác");

    private final String displayName;

    RefundType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RefundType fromCode(String code) {
        for (RefundType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        return HOAN_TIEN;
    }

    public boolean isExchange() {
        return this != HOAN_TIEN;
    }

    public boolean requiresNewVariant() {
        return this == DOI_SIZE || this == DOI_MAU || this == DOI_SAN_PHAM_KHAC;
    }
}