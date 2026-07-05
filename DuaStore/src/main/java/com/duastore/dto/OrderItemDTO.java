package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Integer id;
    private Integer orderId;
    private Integer productId;
    private Integer variantId;
    private String tenSanPham;
    private String tenBienThe;
    private String hinhAnhSP;
    private BigDecimal donGia;
    private Integer soLuong;
    private BigDecimal thanhTien;
    private String loaiGia;
}
