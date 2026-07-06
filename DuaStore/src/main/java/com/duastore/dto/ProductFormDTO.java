/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO dùng cho form Thêm / Sửa sản phẩm (Admin) Nhận dữ liệu từ HTML form — bao
 * gồm cả file upload ảnh chính
 *
 * @author anhpl
 */
@Data
@NoArgsConstructor
public class ProductFormDTO {

    /**
     * null = thêm mới · có giá trị = sửa
     */
    private Integer id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSanPham;

    private String moTa;           // Mô tả chi tiết (HTML)
    private String chatLieu;       // VD: Thủy tinh Borosilicate
    private String xuatXu;         // VD: Việt Nam
    private String mucDichSuDung;  // VD: Đựng đồ uống
    private String thuongHieu;     // VD: Bohemia
    private String kinhLoai;       // VD: Pha lê cắt cạnh

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer danhMucId;

    /**
     * Đường dẫn ảnh hiện tại (khi sửa) — giữ lại nếu không upload mới
     */
    private String hinhAnhChinh;

    /**
     * File ảnh mới upload (tùy chọn)
     */
    private MultipartFile hinhAnhFile;

    /**
     * DANG_BAN | DAT_TRUOC | NGUNG_BAN
     */
    private String trangThaiSanPham = "DANG_BAN";

    /**
     * Số ngày giao khi đặt trước (chỉ dùng khi DAT_TRUOC)
     */
    private Integer leadTimeDays;

    /**
     * true = hiển thị trên trang chủ
     */
    private boolean isFeatured = false;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayPhatHanh;

    /**
     * Gallery images uploaded by admin
     */
    private List<MultipartFile> galleryFiles;
}
