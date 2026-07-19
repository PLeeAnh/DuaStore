package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PricingSuggestionDTO {
    private Integer variantId;
    private String variantName;
    private Integer productId;
    private String productName;

    private BigDecimal currentGiaGoc;
    private BigDecimal currentGiaKhuyenMai;
    private Integer currentStock;

    private String suggestedAction;
    private BigDecimal suggestedGiaGoc;
    private BigDecimal suggestedGiaKhuyenMai;
    private Integer suggestedDiscountPct;

    private String reason;
    private String confidence;
    private int daysUntilEmpty;
    private double salesPerDay;
    private String seasonInfo;
    private boolean actionable;
}
