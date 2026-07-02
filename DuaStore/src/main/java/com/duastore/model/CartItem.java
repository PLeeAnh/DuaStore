package com.duastore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString(exclude = {"product", "variant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "CartItems")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer variantId;

    @Column(nullable = false)
    private Integer soLuong = 1;

    @Column(precision = 12, scale = 0)
    private BigDecimal giaLucThem;

    @Column(updatable = false)
    private LocalDateTime ngayThem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variantId", insertable = false, updatable = false)
    private ProductVariant variant;

    @PrePersist
    protected void onCreate() {
        if (ngayThem == null) {
            ngayThem = LocalDateTime.now();
        }
    }
}
