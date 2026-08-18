package com.duastore.util;

import com.duastore.model.OrderEventType;
import java.util.List;
import java.util.Map;

public final class OrderStatusUtil {

    private static final Map<String, String> DISPLAY_NAMES = Map.ofEntries(
            Map.entry("CHO_XAC_NHAN", "Chờ xác nhận"),
            Map.entry("DA_XAC_NHAN", "Đã xác nhận"),
            Map.entry("DANG_GIAO", "Đang giao"),
            Map.entry("DA_GIAO", "Đã giao"),
            Map.entry("DA_HOAN_THANH", "Hoàn thành"),
            Map.entry("DA_HUY", "Đã hủy"),
            Map.entry("DA_HOAN_TIEN", "Đã hoàn tiền"),
            Map.entry("DA_YEU_CAU_HOAN_TIEN", "Đã yêu cầu hoàn tiền"),
            Map.entry("DANG_TRA_HANG", "Đang trả hàng"),
            Map.entry("DA_NHAN_HANG_TRA", "Đã nhận hàng trả"),
            Map.entry("TU_CHOI_HOAN_TIEN", "Từ chối hoàn tiền")
    );

    private static final List<String> ORDERED_STATUSES = List.of(
            "CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH"
    );

    private static final List<String> REFUND_STATUSES = List.of(
            "DA_YEU_CAU_HOAN_TIEN", "DANG_TRA_HANG", "DA_NHAN_HANG_TRA", "DA_HOAN_TIEN", "TU_CHOI_HOAN_TIEN"
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

    public static List<String> getRefundStatuses() {
        return REFUND_STATUSES;
    }

    public static boolean isRefundStatus(String code) {
        return REFUND_STATUSES.contains(code);
    }

    public static boolean isCompletedOrder(String code) {
        return "DA_GIAO".equals(code) || "DA_HOAN_THANH".equals(code);
    }

    public static boolean isRefundableStatus(String code) {
        return "DA_GIAO".equals(code) || "DA_HOAN_THANH".equals(code);
    }

    public static String getBadgeClass(OrderEventType eventType) {
        if (eventType == null) {
            return "bg-secondary";
        }
        return switch (eventType) {
            case CREATE_ORDER ->
                "bg-secondary";
            case ASSIGN_ADMIN ->
                "bg-info";
            case STATUS_CHANGE ->
                "bg-primary";
            case CANCEL_ORDER ->
                "bg-danger";
            case PAYMENT_CONFIRMED ->
                "bg-success";
            default ->
                "bg-secondary";
        };
    }

    public static String getIconClass(OrderEventType eventType) {
        if (eventType == null) {
            return "bi-record-circle";
        }
        return switch (eventType) {
            case CREATE_ORDER ->
                "bi-plus-circle";
            case ASSIGN_ADMIN ->
                "bi-person-check";
            case STATUS_CHANGE ->
                "bi-arrow-left-right";
            case CANCEL_ORDER ->
                "bi-x-circle";
            case PAYMENT_CONFIRMED ->
                "bi-credit-card";
            default ->
                "bi-record-circle";
        };
    }

    public static String getIconColorClass(OrderEventType eventType) {
        if (eventType == null) {
            return "text-muted";
        }
        return switch (eventType) {
            case CREATE_ORDER ->
                "text-muted";
            case ASSIGN_ADMIN ->
                "text-info";
            case STATUS_CHANGE ->
                "text-primary";
            case CANCEL_ORDER ->
                "text-danger";
            case PAYMENT_CONFIRMED ->
                "text-success";
            default ->
                "text-muted";
        };
    }

    public static String getTagBadgeClass(String tag) {
        if (tag == null || tag.isEmpty()) {
            return "bg-light text-dark";
        }
        return switch (tag) {
            case "Kho" ->
                "bg-warning text-dark";
            case "CSKH" ->
                "bg-info text-dark";
            case "Hệ thống" ->
                "bg-secondary";
            case "Kế toán" ->
                "bg-success";
            default ->
                "bg-light text-dark";
        };
    }

    private OrderStatusUtil() {
    }
}
