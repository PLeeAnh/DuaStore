package com.duastore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class BannerDTO {

    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String tieuDe;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String moTa;

    private String hinhAnh;
    private MultipartFile hinhAnhFile;

    @Size(max = 500, message = "Liên kết tối đa 500 ký tự")
    private String lienKet = "/san-pham";

    @Min(value = 0, message = "Thứ tự phải từ 0 trở lên")
    private Integer thuTu = 0;

    private Boolean isActive = true;
}
