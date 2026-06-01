package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CategoryDTO {

    private Integer id;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String tenDanhMuc;

    private String moTa;

    private Integer parentId;
    private String tenDanhMucCha;

    private Integer thuTuHienThi = 0;
    private boolean isActive = true;

    private List<CategoryDTO> children;
}
