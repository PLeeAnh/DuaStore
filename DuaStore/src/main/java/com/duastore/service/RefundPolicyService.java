package com.duastore.service;

import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.model.ProductVariant;
import com.duastore.model.RefundReason;
import com.duastore.model.RefundType;
import com.duastore.repository.ProductVariantRepository;
import com.duastore.repository.SiteSettingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundPolicyService {

    private final SiteSettingRepository siteSettingRepository;
    private final ProductVariantRepository productVariantRepository;

    public RefundPolicyService(SiteSettingRepository siteSettingRepository,
                               ProductVariantRepository productVariantRepository) {
        this.siteSettingRepository = siteSettingRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public int getReturnWindowDays() {
        return getSettingAsInt("refund_return_window_days", 7);
    }

    public boolean isCustomGlassNonRefundable() {
        return getSettingAsBoolean("refund_custom_non_refundable", true);
    }

    public boolean isFlashSaleExchangeOnly() {
        return getSettingAsBoolean("refund_flash_sale_exchange_only", true);
    }

    public boolean isFreeExchangeShippingForFlashSale() {
        return getSettingAsBoolean("refund_flash_sale_free_exchange_shipping", true);
    }

    public boolean requiresVideoProofForGlass() {
        return getSettingAsBoolean("refund_require_video_proof_glass", true);
    }

    public BigDecimal getMaxRefundRateForDamaged() {
        return getSettingAsDecimal("refund_max_rate_damaged", "0.8");
    }

    public boolean validateRefundEligibility(Order order, RefundType type, RefundReason reason) {
        if (!isRefundableStatus(order.getTrangThaiDon())) {
            return false;
        }

        LocalDateTime deliveryDate = getDeliveryDate(order);
        if (deliveryDate == null) {
            return false;
        }

        int daysSinceDelivery = (int) java.time.Duration.between(deliveryDate, LocalDateTime.now()).toDays();
        if (daysSinceDelivery > getReturnWindowDays()) {
            return false;
        }

        for (OrderItem item : order.getOrderItems()) {
            if (item.getVariantId() != null) {
                ProductVariant variant = productVariantRepository.findById(item.getVariantId()).orElse(null);
                if (variant != null) {
                    if (isCustomGlassNonRefundable() && variant.isCustom()) {
                        return type == RefundType.DOI_SIZE || type == RefundType.DOI_MAU;
                    }
                    if (isFlashSaleVariant(variant) && isFlashSaleExchangeOnly()) {
                        return type == RefundType.DOI_SIZE;
                    }
                }
            }
        }

        return true;
    }

    public BigDecimal calculateRefundAmount(Order order, RefundType type, RefundReason reason, BigDecimal shippingFee) {
        BigDecimal baseAmount = order.getTongThanhToan();
        BigDecimal voucherDiscount = order.getTienGiam();
        BigDecimal actualPaid = baseAmount.subtract(voucherDiscount);

        if (type == RefundType.HOAN_TIEN) {
            if (reason.isShopFault()) {
                return actualPaid;
            } else {
                return actualPaid.subtract(shippingFee);
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal calculateExchangePriceDiff(OrderItem oldItem, ProductVariant newVariant) {
        BigDecimal oldPrice = oldItem.getDonGia();
        BigDecimal newPrice = newVariant.getGiaKhuyenMai() != null ? newVariant.getGiaKhuyenMai() : newVariant.getGiaGoc();
        return newPrice.subtract(oldPrice).multiply(BigDecimal.valueOf(oldItem.getSoLuong()));
    }

    public boolean isRefundableStatus(String orderStatus) {
        return "DA_GIAO".equals(orderStatus) || "DA_HOAN_THANH".equals(orderStatus);
    }

    private LocalDateTime getDeliveryDate(Order order) {
        if ("DA_GIAO".equals(order.getTrangThaiDon()) || "DA_HOAN_THANH".equals(order.getTrangThaiDon())) {
            if (order.getNgayGiao() != null) {
                return order.getNgayGiao();
            }
            return order.getNgayCapNhat() != null ? order.getNgayCapNhat() : order.getNgayDat();
        }
        return null;
    }

    private boolean isFlashSaleVariant(ProductVariant variant) {
        return variant.getGiaKhuyenMai() != null
                && variant.getGiaKhuyenMai().compareTo(BigDecimal.ZERO) > 0
                && variant.getGiaKhuyenMai().compareTo(variant.getGiaGoc()) < 0;
    }

    private int getSettingAsInt(String key, int defaultValue) {
        try {
            return siteSettingRepository.findBySettingKey(key).map(s -> Integer.parseInt(s.getSettingValue())).orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getSettingAsBoolean(String key, boolean defaultValue) {
        try {
            return siteSettingRepository.findBySettingKey(key).map(s -> Boolean.parseBoolean(s.getSettingValue())).orElse(defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private BigDecimal getSettingAsDecimal(String key, String defaultValue) {
        try {
            return siteSettingRepository.findBySettingKey(key).map(s -> new BigDecimal(s.getSettingValue())).orElse(new BigDecimal(defaultValue));
        } catch (Exception e) {
            return new BigDecimal(defaultValue);
        }
    }
}