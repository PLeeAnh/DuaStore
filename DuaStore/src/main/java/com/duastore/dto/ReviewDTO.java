package com.duastore.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    private Integer id;
    private Integer productId;
    private String tenSanPham;
    private Integer userId;
    private String hoTen;
    private Integer danhGia;
    private String binhLuan;
    private boolean isApproved;
    private LocalDateTime ngayTao;
}
