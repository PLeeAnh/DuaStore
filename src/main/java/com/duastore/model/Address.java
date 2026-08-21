package com.duastore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Addresses")
/**
 * Entity ánh xạ dữ liệu địa chỉ giao hàng.
 */
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "userId", nullable = false)
    private Integer userId;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    @Column(name = "tenNguoiNhan", nullable = false, length = 100)
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (VD: 0901234567 hoặc +84901234567)")
    @Size(min = 10, max = 15, message = "Số điện thoại từ 10-15 ký tự")
    @Column(name = "soDienThoai", nullable = false, length = 15)
    private String soDienThoai;

    @NotBlank(message = "Tỉnh/Thành không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành tối đa 100 ký tự")
    @Column(name = "tinhThanh", nullable = false, length = 100)
    private String tinhThanh;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện tối đa 100 ký tự")
    @Column(name = "quanHuyen", nullable = false, length = 100)
    private String quanHuyen;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100, message = "Phường/Xã tối đa 100 ký tự")
    @Column(name = "phuongXa", nullable = false, length = 100)
    private String phuongXa;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 200, message = "Địa chỉ cụ thể tối đa 200 ký tự")
    @Column(name = "diaChiCuThe", nullable = false, length = 200)
    private String diaChiCuThe;

    @Column(name = "isDefault", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "ghnDistrictId")
    private Integer ghnDistrictId;

    @Column(name = "ghnWardCode", length = 20)
    private String ghnWardCode;
}
