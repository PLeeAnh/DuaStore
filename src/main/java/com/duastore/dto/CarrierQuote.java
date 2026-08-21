package com.duastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu carrier quote giữa các tầng controller/service/view.
 */
public class CarrierQuote {
    private String carrierCode;
    private String carrierName;
    private BigDecimal fee;
    private int deliveryDays;
    private boolean isEstimated;
}
