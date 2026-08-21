package com.duastore.model;

/**
 * Enum liệt kê các giá trị hợp lệ cho đơn hàng.
 */
public enum OrderEventType {
    CREATE_ORDER,
    ASSIGN_ADMIN,
    STATUS_CHANGE,
    CANCEL_ORDER,
    PAYMENT_CONFIRMED,
    REFUND_ORDER
}
