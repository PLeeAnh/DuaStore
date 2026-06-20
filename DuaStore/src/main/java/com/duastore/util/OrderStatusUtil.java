package com.duastore.util;

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

    private OrderStatusUtil() {}
}
