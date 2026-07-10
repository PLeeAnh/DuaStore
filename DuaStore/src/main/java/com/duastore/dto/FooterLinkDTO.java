package com.duastore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class FooterLinkDTO {

    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề tối đa 200 ký tự")
    private String title;

    @NotBlank(message = "Đường dẫn không được để trống")
    @Size(max = 500, message = "Đường dẫn tối đa 500 ký tự")
    private String url;

    @Min(value = 0, message = "Thứ tự phải từ 0 trở lên")
    private Integer displayOrder = 0;

    private Boolean isActive = true;

    @Min(value = 1, message = "Cột phải từ 1 đến 4")
    private Integer columnIndex = 1;

    private LocalDateTime createdAt;
}
