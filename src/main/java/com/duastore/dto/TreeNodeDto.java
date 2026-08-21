package com.duastore.dto;

import lombok.Data;

@Data
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu tree node dto giữa các tầng controller/service/view.
 */
public class TreeNodeDto {

    private Integer id;
    private String tenDanhMuc;
    private String imageUrl;
    private boolean active;
    private Integer thuTuHienThi;
    private boolean hasChildren;
    private int level;
    private String fullPath;
    private long productCount;
    private int childCount;
}
