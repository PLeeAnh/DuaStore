package com.duastore.util;

import com.duastore.model.OrderEventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusUtilTest {

    @ParameterizedTest
    @CsvSource({
            "CHO_XAC_NHAN, Chờ xác nhận",
            "DA_XAC_NHAN, Đã xác nhận",
            "DANG_GIAO, Đang giao",
            "DA_GIAO, Đã giao",
            "DA_HOAN_THANH, Hoàn thành",
            "DA_HUY, Đã hủy"
    })
    void getDisplayName_validStatus_returnsVietnameseLabel(String code, String expected) {
        assertThat(OrderStatusUtil.getDisplayName(code)).isEqualTo(expected);
    }

    @Test
    void getDisplayName_unknownCode_fallsBackToRawCode() {
        // Realistic case: an old refund status still lingering in historical data
        // (order_status_logs, admin_action_logs) after the refund feature was removed.
        assertThat(OrderStatusUtil.getDisplayName("DA_HOAN_TIEN")).isEqualTo("DA_HOAN_TIEN");
    }

    @Test
    void getDisplayName_nullCode_returnsNull() {
        assertThat(OrderStatusUtil.getDisplayName(null)).isNull();
    }

    @Test
    void getOrderedStatuses_doesNotContainCancelledOrRefundStatuses() {
        // DA_HUY (cancelled) and any refund status must never appear on the
        // step-progress bar shown on the order detail page.
        assertThat(OrderStatusUtil.getOrderedStatuses())
                .containsExactly("CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_GIAO", "DA_HOAN_THANH")
                .doesNotContain("DA_HUY", "DA_HOAN_TIEN", "DANG_TRA_HANG");
    }

    @ParameterizedTest
    @CsvSource({
            "CHO_XAC_NHAN, 1",
            "DA_XAC_NHAN, 2",
            "DANG_GIAO, 3",
            "DA_GIAO, 4",
            "DA_HOAN_THANH, 5"
    })
    void getStepIndex_knownStatus_returnsOneBasedPosition(String code, int expectedStep) {
        assertThat(OrderStatusUtil.getStepIndex(code)).isEqualTo(expectedStep);
    }

    @Test
    void getStepIndex_cancelledOrder_returnsZero() {
        // DA_HUY is intentionally not part of the linear progress bar.
        assertThat(OrderStatusUtil.getStepIndex("DA_HUY")).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DA_GIAO", "DA_HOAN_THANH"})
    void isCompletedOrder_deliveredOrFinished_returnsTrue(String code) {
        assertThat(OrderStatusUtil.isCompletedOrder(code)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHO_XAC_NHAN", "DA_XAC_NHAN", "DANG_GIAO", "DA_HUY"})
    void isCompletedOrder_notYetDelivered_returnsFalse(String code) {
        assertThat(OrderStatusUtil.isCompletedOrder(code)).isFalse();
    }

    @Test
    void getBadgeClass_nullEventType_returnsSecondaryDefault() {
        assertThat(OrderStatusUtil.getBadgeClass(null)).isEqualTo("bg-secondary");
    }

    @Test
    void getBadgeClass_cancelOrder_returnsDangerColor() {
        assertThat(OrderStatusUtil.getBadgeClass(OrderEventType.CANCEL_ORDER)).isEqualTo("bg-danger");
        assertThat(OrderStatusUtil.getIconColorClass(OrderEventType.CANCEL_ORDER)).isEqualTo("text-danger");
    }

    @ParameterizedTest
    @CsvSource({
            "Kho, bg-warning text-dark",
            "CSKH, bg-info text-dark",
            "Hệ thống, bg-secondary",
            "Kế toán, bg-success"
    })
    void getTagBadgeClass_knownOrderNoteTag_returnsMatchingClass(String tag, String expected) {
        // These are exactly the 4 tags seeded into order_notes — a typo here would
        // silently render every admin note with the generic fallback style.
        assertThat(OrderStatusUtil.getTagBadgeClass(tag)).isEqualTo(expected);
    }

    @Test
    void getTagBadgeClass_unknownTag_returnsLightFallback() {
        assertThat(OrderStatusUtil.getTagBadgeClass("Marketing")).isEqualTo("bg-light text-dark");
    }

    @Test
    void getTagBadgeClass_nullOrEmptyTag_returnsLightFallback() {
        assertThat(OrderStatusUtil.getTagBadgeClass(null)).isEqualTo("bg-light text-dark");
        assertThat(OrderStatusUtil.getTagBadgeClass("")).isEqualTo("bg-light text-dark");
    }
}
