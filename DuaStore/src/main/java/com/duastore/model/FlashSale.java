package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FlashSales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(nullable = false)
    private Integer soLuongDaBan = 0;

    @Column(nullable = false)
    private Integer soLuongToiDa;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (soLuongDaBan == null) {
            soLuongDaBan = 0;
        }
    }
}
