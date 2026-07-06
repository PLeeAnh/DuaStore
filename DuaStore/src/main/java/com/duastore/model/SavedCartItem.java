package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString(exclude = {"product", "variant"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "SavedCartItems")
public class SavedCartItem {

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

    @Column(nullable = false)
    private BigDecimal giaLuu;

    @Column(updatable = false)
    private LocalDateTime ngayLuu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variantId", insertable = false, updatable = false)
    private ProductVariant variant;

    @PrePersist
    protected void onCreate() {
        if (ngayLuu == null) {
            ngayLuu = LocalDateTime.now();
        }
    }
}
