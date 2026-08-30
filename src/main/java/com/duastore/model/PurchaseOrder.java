package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PurchaseOrders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String maPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplierId", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, length = 30)
    private String trangThai = "CHO_DUYET";

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal tongTien = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal soTienDaTra = BigDecimal.ZERO;

    @Column
    private LocalDateTime ngayNhap;

    @Column
    private LocalDateTime ngayDuyet;

    @Column
    private LocalDateTime ngayHoanThanh;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @Column(nullable = false)
    private Integer createdBy;

    @Column
    private Integer approvedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
