package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu thanh toán/đặt hàng (checkout) giữa các tầng controller/service/view.
 */
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

    /** Idempotency key chong dat hang trung (client phat sinh UUID khi mo trang checkout). */
    private String idempotencyKey;
}
