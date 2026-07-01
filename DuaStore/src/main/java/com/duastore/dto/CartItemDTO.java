package com.duastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer variantId;
    private String tenSanPham;
    private String tenBienThe;
    private String hinhAnh;
    private BigDecimal giaBan;
    private BigDecimal giaBanSauGiam;
    private Integer soLuong;
    private Integer soLuongTon;
    private BigDecimal thanhTien;
    private LocalDateTime ngayThem;
}
