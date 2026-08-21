package com.duastore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu đánh giá sản phẩm giữa các tầng controller/service/view.
 */
public class ReviewRequestDTO {

    private Integer productId;

    @NotNull(message = "Vui lòng chọn số sao")
    @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa 5 sao")
    private Integer danhGia;

    private String binhLuan;
}
