package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PurchaseOrderItems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchaseOrderId", nullable = false)
    private PurchaseOrder purchaseOrder;

    private Integer variantId;

    @Column(nullable = false, length = 300)
    private String tenSanPham;

    @Column(nullable = false)
    private Integer soLuong = 0;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal giaNhap = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal thanhTien = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer soLuongNhan = 0;

    @Column(length = 500)
    private String ghiChu;
}
