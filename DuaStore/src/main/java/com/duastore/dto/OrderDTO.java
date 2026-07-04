package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Integer id;
    private String maDon;
    private Integer userId;
    private String tenNguoiNhan;
    private String soDienThoai;
    private String diaChi;
    private BigDecimal tienHang;
    private BigDecimal phiVanChuyen;
    private BigDecimal tienGiam;
    private BigDecimal tongThanhToan;
    private String phuongThucTT;
    private String phuongThucGiaoHang;
    private String trangThaiTT;
    private String trangThaiDon;
    private Integer promotionId;
    private String ghiChu;
    private String maVanDon;
    private LocalDateTime ngayDat;
    private String userEmail;
}
