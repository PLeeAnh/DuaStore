package com.duastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarrierQuote {
    private String carrierCode;
    private String carrierName;
    private BigDecimal fee;
    private int deliveryDays;
    private boolean isEstimated;
}
