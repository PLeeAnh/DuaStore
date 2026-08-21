package com.duastore.dto;

import com.duastore.model.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu danh mục giữa các tầng controller/service/view.
 */
public class NavMenuCategory {

    private Integer id;
    private String tenDanhMuc;
    private List<NavMenuCategory> children = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
}
