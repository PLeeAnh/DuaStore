package com.duastore.dto;

import com.duastore.model.Product;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NavMenuCategory {

    private Integer id;
    private String tenDanhMuc;
    private List<NavMenuCategory> children = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
}
