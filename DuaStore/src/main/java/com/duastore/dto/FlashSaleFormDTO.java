package com.duastore.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FlashSaleFormDTO {

    private Integer id;

    @NotNull(message = "Sản phẩm không được để trống")
    private Integer productId;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    private Boolean isActive = true;

    private Integer soLuongDaBan;

    private Integer soLuongToiDa;

    @AssertTrue(message = "Ngày bắt đầu phải trước ngày kết thúc")
    public boolean isDateRangeValid() {
        return ngayBatDau == null || ngayKetThuc == null || ngayBatDau.isBefore(ngayKetThuc);
    }
}
