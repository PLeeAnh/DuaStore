package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(exclude = {"variants", "galleryImages"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
    private String hinhDang;

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
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductVariant> variants;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    @SQLRestriction("isActive = 1")
    @jakarta.persistence.OrderBy("sortOrder ASC")
    private List<ProductImage> galleryImages;
}
