package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderStatusDTO {

    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    private String trangThaiDon;

    private String trangThaiTT = "CHUA_THANH_TOAN";
}
