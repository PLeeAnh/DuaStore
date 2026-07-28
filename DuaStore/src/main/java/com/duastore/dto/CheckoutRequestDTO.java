package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class CheckoutRequestDTO {

    @NotNull(message = "Vui lòng chọn địa chỉ giao hàng")
    private Integer addressId;

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String phuongThucTT;

    private String phuongThucGiaoHang = "SHIP";

    private String shippingCarrier = "GHN";

    private String maCode;

    private String ghiChu;

    private Integer pointsToRedeem;

    private List<Integer> selectedIds;
}
