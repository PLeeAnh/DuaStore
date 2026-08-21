package com.duastore.config.security;

/**
 * Lớp hỗ trợ xử lý quyền hạn (permission).
 */
public final class PermissionEnum {

    private PermissionEnum() {
    }

    // DASHBOARD
    public static final String DASHBOARD_READ = "DASHBOARD_READ";

    // PRODUCT
    public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
    public static final String PRODUCT_READ = "PRODUCT_READ";
    public static final String PRODUCT_UPDATE = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE = "PRODUCT_DELETE";

    // ORDER
    public static final String ORDER_READ = "ORDER_READ";
    public static final String ORDER_UPDATE = "ORDER_UPDATE";

    // USER
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_UPDATE = "USER_UPDATE";

    // CATEGORY
    public static final String CATEGORY_CREATE = "CATEGORY_CREATE";
    public static final String CATEGORY_READ = "CATEGORY_READ";
    public static final String CATEGORY_UPDATE = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE = "CATEGORY_DELETE";

    // PROMOTION
    public static final String PROMOTION_CREATE = "PROMOTION_CREATE";
    public static final String PROMOTION_READ = "PROMOTION_READ";
    public static final String PROMOTION_UPDATE = "PROMOTION_UPDATE";
    public static final String PROMOTION_DELETE = "PROMOTION_DELETE";

    // REVIEW
    public static final String REVIEW_READ = "REVIEW_READ";
    public static final String REVIEW_APPROVE = "REVIEW_APPROVE";
    public static final String REVIEW_HIDE = "REVIEW_HIDE";
    public static final String REVIEW_DELETE = "REVIEW_DELETE";

    // POST
    public static final String POST_CREATE = "POST_CREATE";
    public static final String POST_READ = "POST_READ";
    public static final String POST_UPDATE = "POST_UPDATE";
    public static final String POST_DELETE = "POST_DELETE";

    // POST_CATEGORY
    public static final String POST_CATEGORY_CREATE = "POST_CATEGORY_CREATE";
    public static final String POST_CATEGORY_READ = "POST_CATEGORY_READ";
    public static final String POST_CATEGORY_UPDATE = "POST_CATEGORY_UPDATE";
    public static final String POST_CATEGORY_DELETE = "POST_CATEGORY_DELETE";

    // VARIANT
    public static final String VARIANT_CREATE = "VARIANT_CREATE";
    public static final String VARIANT_READ = "VARIANT_READ";
    public static final String VARIANT_UPDATE = "VARIANT_UPDATE";
    public static final String VARIANT_DELETE = "VARIANT_DELETE";

    // ROLE
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";

    // NOTIFICATION
    public static final String NOTIFICATION_CREATE = "NOTIFICATION_CREATE";
    public static final String NOTIFICATION_READ = "NOTIFICATION_READ";
    public static final String NOTIFICATION_UPDATE = "NOTIFICATION_UPDATE";
    public static final String NOTIFICATION_DELETE = "NOTIFICATION_DELETE";

    // AUDIT_LOG
    public static final String AUDIT_LOG_READ = "AUDIT_LOG_READ";

    // STORE (address management)
    public static final String STORE_CREATE = "STORE_CREATE";
    public static final String STORE_READ = "STORE_READ";
    public static final String STORE_UPDATE = "STORE_UPDATE";
    public static final String STORE_DELETE = "STORE_DELETE";

    // BANNER
    public static final String BANNER_CREATE = "BANNER_CREATE";
    public static final String BANNER_READ = "BANNER_READ";
    public static final String BANNER_UPDATE = "BANNER_UPDATE";
    public static final String BANNER_DELETE = "BANNER_DELETE";

    // CUSTOMER
    public static final String CUSTOMER_READ = "CUSTOMER_READ";
    public static final String CUSTOMER_UPDATE = "CUSTOMER_UPDATE";

    // HOMEPAGE
    public static final String HOMEPAGE_READ = "HOMEPAGE_READ";
    public static final String HOMEPAGE_UPDATE = "HOMEPAGE_UPDATE";

    // APPEARANCE (Giao diện)
    public static final String APPEARANCE_READ = "APPEARANCE_READ";
    public static final String APPEARANCE_UPDATE = "APPEARANCE_UPDATE";

    // ANALYTICS (Phân tích)
    public static final String ANALYTICS_READ = "ANALYTICS_READ";

    // EMAIL_SETTING
    public static final String EMAIL_SETTING_READ = "EMAIL_SETTING_READ";
    public static final String EMAIL_SETTING_UPDATE = "EMAIL_SETTING_UPDATE";

    // PAYMENT_SETTING
    public static final String PAYMENT_SETTING_READ = "PAYMENT_SETTING_READ";
    public static final String PAYMENT_SETTING_UPDATE = "PAYMENT_SETTING_UPDATE";

    // SHIPPING_SETTING
    public static final String SHIPPING_SETTING_READ = "SHIPPING_SETTING_READ";
    public static final String SHIPPING_SETTING_UPDATE = "SHIPPING_SETTING_UPDATE";

    // FLASH_SALE
    public static final String FLASH_SALE_CREATE = "FLASH_SALE_CREATE";
    public static final String FLASH_SALE_READ = "FLASH_SALE_READ";
    public static final String FLASH_SALE_UPDATE = "FLASH_SALE_UPDATE";
    public static final String FLASH_SALE_DELETE = "FLASH_SALE_DELETE";

    // REFUND
    public static final String REFUND_READ = "REFUND_READ";
    public static final String REFUND_UPDATE = "REFUND_UPDATE";
    public static final String REFUND_APPROVE = "REFUND_APPROVE";

    // FOOTER_LINK
    public static final String FOOTER_LINK_READ = "FOOTER_LINK_READ";
    public static final String FOOTER_LINK_CREATE = "FOOTER_LINK_CREATE";
    public static final String FOOTER_LINK_UPDATE = "FOOTER_LINK_UPDATE";
    public static final String FOOTER_LINK_DELETE = "FOOTER_LINK_DELETE";

    // PRICE_HISTORY
    public static final String PRICE_HISTORY_READ = "PRICE_HISTORY_READ";

    // LOYALTY
    public static final String LOYALTY_READ = "LOYALTY_READ";
    public static final String LOYALTY_UPDATE = "LOYALTY_UPDATE";

    // ALERT
    public static final String ALERT_READ = "ALERT_READ";
    public static final String ALERT_UPDATE = "ALERT_UPDATE";

    // CONTACT_MESSAGE (Hộp thư liên hệ)
    public static final String CONTACT_MESSAGE_READ = "CONTACT_MESSAGE_READ";
    public static final String CONTACT_MESSAGE_UPDATE = "CONTACT_MESSAGE_UPDATE";
    public static final String CONTACT_MESSAGE_DELETE = "CONTACT_MESSAGE_DELETE";
}
