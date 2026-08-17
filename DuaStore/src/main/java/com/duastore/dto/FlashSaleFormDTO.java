package com.duastore.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class FlashSaleFormDTO {

    private Integer id;

    @NotBlank(message = "Tên chương trình không được để trống")
    private String tenChuongTrinh;

    private String moTa;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    private Boolean isActive = true;

    @Min(value = 0, message = "Ưu tiên không hợp lệ")
    private Integer priority = 0;

    @AssertTrue(message = "Ngày bắt đầu phải trước ngày kết thúc")
    public boolean isDateRangeValid() {
        return ngayBatDau == null || ngayKetThuc == null || ngayBatDau.isBefore(ngayKetThuc);
    }

    private List<FlashSaleItemFormDTO> items = new ArrayList<>();
}