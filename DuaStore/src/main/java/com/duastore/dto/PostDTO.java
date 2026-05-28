package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PostDTO {

    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String tieuDe;

    private String tomTat;
    private String noiDung;

    private String hinhAnh;
    private MultipartFile hinhAnhFile;

    private Integer tacGiaId;
    private String trangThai = "NHAP";
    private Integer luotXem;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}
