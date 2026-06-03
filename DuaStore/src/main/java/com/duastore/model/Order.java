package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "promotion", "orderItems"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 20)
    private String maDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column
    private Integer addressId;

    @Column(nullable = false, length = 100)
    private String snapTenNguoiNhan;

    @Column(nullable = false, length = 15)
    private String snapSoDienThoai;

    @Column(nullable = false, length = 500)
    private String snapDiaChi;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal tienHang;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal phiVanChuyen;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal tienGiam;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal tongThanhToan;

    @Column(nullable = false, length = 20)
    private String phuongThucTT;

    @Column(nullable = false, length = 20)
    private String phuongThucGiaoHang;

    @Column(nullable = false, length = 25)
    private String trangThaiTT;

    @Column(nullable = false, length = 20)
    private String trangThaiDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotionId")
    private Promotion promotion;

    @Column(length = 500)
    private String ghiChu;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayDat;

    @Column
    private LocalDateTime ngayCapNhat;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        ngayDat = LocalDateTime.now();
        if (phiVanChuyen == null) phiVanChuyen = BigDecimal.ZERO;
        if (tienGiam == null) tienGiam = BigDecimal.ZERO;
        if (trangThaiTT == null) trangThaiTT = "CHUA_THANH_TOAN";
        if (trangThaiDon == null) trangThaiDon = "CHO_XAC_NHAN";
        if (phuongThucGiaoHang == null) phuongThucGiaoHang = "SHIP";
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
