package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {

    private Integer id;
    private Integer userId;
    private Integer productId;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaBan;
    private BigDecimal giaGoc;
    private String trangThaiSanPham;
    private LocalDateTime ngayThem;
}
