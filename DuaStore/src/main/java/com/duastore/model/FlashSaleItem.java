package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "FlashSaleItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashSaleId", nullable = false)
    private FlashSale flashSale;

    @Column(nullable = false)
    private Integer variantId;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal giaGoc;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal giaSale;

    @Column(nullable = false)
    private Integer soLuongToiDa = 0;

    @Column(nullable = false)
    private Integer soLuongDaBan = 0;

    @Column(nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (soLuongToiDa == null) {
            soLuongToiDa = 0;
        }
        if (soLuongDaBan == null) {
            soLuongDaBan = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    public Integer getSoLuongConLai() {
        int toiDa = soLuongToiDa == null ? 0 : soLuongToiDa;
        int daBan = soLuongDaBan == null ? 0 : soLuongDaBan;
        return Math.max(0, toiDa - daBan);
    }

    public int getPercentSold() {
        if (soLuongToiDa == null || soLuongToiDa <= 0) {
            return 0;
        }
        int daBan = soLuongDaBan == null ? 0 : soLuongDaBan;
        return Math.min(100, (int) Math.floor(daBan * 100.0 / soLuongToiDa));
    }

    public boolean isSoldOut() {
        return getSoLuongConLai() <= 0;
    }
}
