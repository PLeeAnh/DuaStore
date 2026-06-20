package com.duastore.util;

import com.duastore.model.OrderEventType;
import java.util.List;
import java.util.Map;

public final class OrderStatusUtil {

    private static final Map<String, String> DISPLAY_NAMES = Map.of(
        "CHO_XAC_NHAN", "Chờ xác nhận",
        "DA_XAC_NHAN", "Đã xác nhận",
        "DANG_GIAO", "Đang giao",
        "DA_GIAO", "Đã giao",
        "DA_HOAN_THANH", "Hoàn thành",
        "DA_HUY", "Đã hủy"
    );

    private static final List<String> ORDERED_STATUSES = List.of(
        "CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH"
    );

    public static String getDisplayName(String code) {
        return DISPLAY_NAMES.getOrDefault(code, code);
    }

    public static int getStepIndex(String code) {
        return ORDERED_STATUSES.indexOf(code) + 1;
    }

    public static List<String> getOrderedStatuses() {
        return ORDERED_STATUSES;
    }

    public static int getTotalSteps() {
        return ORDERED_STATUSES.size();
    }

    public static String getBadgeClass(OrderEventType eventType) {
        if (eventType == null) return "bg-secondary";
        return switch (eventType) {
            case CREATE_ORDER -> "bg-primary";
            case ASSIGN_ADMIN -> "bg-info";
            case STATUS_CHANGE -> "bg-success";
            case CANCEL_ORDER -> "bg-danger";
            default -> "bg-secondary";
        };
    }

    public static String getIconClass(OrderEventType eventType) {
        if (eventType == null) return "bi-clock";
        return switch (eventType) {
            case CREATE_ORDER -> "bi-cart-plus";
            case ASSIGN_ADMIN -> "bi-person-check";
            case STATUS_CHANGE -> "bi-arrow-left-right";
            case CANCEL_ORDER -> "bi-x-circle";
            case PAYMENT_CONFIRMED -> "bi-credit-card";
            default -> "bi-clock";
        };
    }

    private OrderStatusUtil() {}
}
