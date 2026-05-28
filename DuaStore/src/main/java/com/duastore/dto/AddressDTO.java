package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Integer id;
    private Integer userId;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String diaChiCuThe;
    private boolean isDefault;
}
