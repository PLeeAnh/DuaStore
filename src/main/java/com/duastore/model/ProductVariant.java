package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@ToString(exclude = {"product"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "ProductVariants")
/**
 * Entity ánh xạ dữ liệu sản phẩm, biến thể sản phẩm.
 */
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private String tenBienThe;

    private Integer dungTich;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal giaGoc;

    @Column(precision = 12, scale = 0)
    private BigDecimal giaKhuyenMai;

    @Column(nullable = false)
    private Integer soLuongTon = 0;

    private String hinhAnh;
    private boolean isDefault = false;
    private boolean isActive = true;
    private boolean isCustom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;
}
