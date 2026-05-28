package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressRequestDTO {

    @NotBlank(message = "Tên người nhận không được để trống")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String soDienThoai;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String tinhThanh;

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String quanHuyen;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String phuongXa;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String diaChiCuThe;

    private boolean isDefault = false;
}
