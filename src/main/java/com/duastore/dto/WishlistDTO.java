package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu danh sách yêu thích giữa các tầng controller/service/view.
 */
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
