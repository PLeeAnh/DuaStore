package com.duastore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PromotionDTO {

    private Integer id;

    @NotBlank(message = "Mã code không được để trống")
    private String maCode;

    @NotBlank(message = "Tên chương trình không được để trống")
    private String tenChuongTrinh;

    @NotBlank(message = "Loại giảm không được để trống")
    private String loaiGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0", message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal donHangToiThieu;

    @DecimalMin(value = "0", message = "Giảm tối đa không được âm")
    private BigDecimal giamToiDa;

    private Integer soLanDung;
    private Integer daDung;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime tuNgay;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime denNgay;

    private boolean isActive = true;
}
