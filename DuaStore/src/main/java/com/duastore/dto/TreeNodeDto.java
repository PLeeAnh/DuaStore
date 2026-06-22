package com.duastore.dto;

import lombok.Data;

@Data
public class TreeNodeDto {
    private Integer id;
    private String tenDanhMuc;
    private String imageUrl;
    private boolean active;
    private Integer thuTuHienThi;
    private boolean hasChildren;
    private int level;
    private long productCount;
}
