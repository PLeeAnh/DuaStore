package com.duastore.config.security;

public final class PermissionEnum {

    private PermissionEnum() {}

    // DASHBOARD
    public static final String DASHBOARD_READ   = "DASHBOARD_READ";

    // PRODUCT
    public static final String PRODUCT_CREATE   = "PRODUCT_CREATE";
    public static final String PRODUCT_READ     = "PRODUCT_READ";
    public static final String PRODUCT_UPDATE   = "PRODUCT_UPDATE";
    public static final String PRODUCT_DELETE   = "PRODUCT_DELETE";

    // ORDER
    public static final String ORDER_READ       = "ORDER_READ";
    public static final String ORDER_UPDATE     = "ORDER_UPDATE";

    // USER
    public static final String USER_READ        = "USER_READ";
    public static final String USER_UPDATE      = "USER_UPDATE";

    // CATEGORY
    public static final String CATEGORY_CREATE  = "CATEGORY_CREATE";
    public static final String CATEGORY_READ    = "CATEGORY_READ";
    public static final String CATEGORY_UPDATE  = "CATEGORY_UPDATE";
    public static final String CATEGORY_DELETE  = "CATEGORY_DELETE";

    // PROMOTION
    public static final String PROMOTION_CREATE = "PROMOTION_CREATE";
    public static final String PROMOTION_READ   = "PROMOTION_READ";
    public static final String PROMOTION_UPDATE = "PROMOTION_UPDATE";
    public static final String PROMOTION_DELETE = "PROMOTION_DELETE";

    // REVIEW
    public static final String REVIEW_READ      = "REVIEW_READ";
    public static final String REVIEW_APPROVE   = "REVIEW_APPROVE";
    public static final String REVIEW_HIDE      = "REVIEW_HIDE";
    public static final String REVIEW_DELETE    = "REVIEW_DELETE";

    // POST
    public static final String POST_CREATE      = "POST_CREATE";
    public static final String POST_READ        = "POST_READ";
    public static final String POST_UPDATE      = "POST_UPDATE";
    public static final String POST_DELETE      = "POST_DELETE";

    // VARIANT
    public static final String VARIANT_CREATE   = "VARIANT_CREATE";
    public static final String VARIANT_READ     = "VARIANT_READ";
    public static final String VARIANT_UPDATE   = "VARIANT_UPDATE";
    public static final String VARIANT_DELETE   = "VARIANT_DELETE";

    // ROLE
    public static final String ROLE_CREATE      = "ROLE_CREATE";
    public static final String ROLE_READ        = "ROLE_READ";
    public static final String ROLE_UPDATE      = "ROLE_UPDATE";
    public static final String ROLE_DELETE      = "ROLE_DELETE";

    // AUDIT_LOG
    public static final String AUDIT_LOG_READ   = "AUDIT_LOG_READ";

    // NOTIFICATION
    public static final String NOTIFICATION_CREATE = "NOTIFICATION_CREATE";
    public static final String NOTIFICATION_READ   = "NOTIFICATION_READ";
    public static final String NOTIFICATION_UPDATE = "NOTIFICATION_UPDATE";
    public static final String NOTIFICATION_DELETE = "NOTIFICATION_DELETE";
}
