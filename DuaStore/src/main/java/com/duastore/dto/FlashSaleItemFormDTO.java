package com.duastore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class FlashSaleItemFormDTO {

    private Integer id;

    private Integer flashSaleId;

    @NotNull(message = "Biến thể không được để trống")
    private Integer variantId;

    private Integer productId;

    private BigDecimal giaGoc;

    @NotNull(message = "Giá sale không được để trống")
    @DecimalMin(value = "1", message = "Giá sale phải lớn hơn 0")
    private BigDecimal giaSale;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuongToiDa;

    private Boolean isActive = true;
}