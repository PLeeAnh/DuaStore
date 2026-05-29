package com.duastore.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "Products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String tenSanPham;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    private String chatLieu;
    private String xuatXu;
    private String mucDichSuDung;
    private String thuongHieu;
    private String kinhLoai;

    @Column(nullable = false)
    private Integer danhMucId;

    private String hinhAnhChinh;

    @Column(nullable = false)
    private String trangThaiSanPham = "DANG_BAN";

    private Integer leadTimeDays;
    private boolean isFeatured = false;
    private boolean isActive = true;

    @Column(updatable = false)
    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() { ngayTao = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { ngayCapNhat = LocalDateTime.now(); }

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductVariant> variants;
}
