package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DynamicUpdate
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "promotion", "orderItems"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu đơn hàng.
 */
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @Column(length = 50)
    private String maVanDon;

    @Column(length = 100)
    private String sepayTransactionId;

    @Column(length = 1000)
    private String fraudWarning;

    @Column(length = 30)
    private String shippingCarrier;

    @CreatedBy
    @Column(updatable = false)
    private Integer createdBy;

    @LastModifiedBy
    private Integer lastModifiedBy;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime ngayDat;

    @Column
    @LastModifiedDate
    private LocalDateTime ngayCapNhat;

    @Column
    private LocalDateTime ngayGiao;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        ngayDat = LocalDateTime.now();
        if (phiVanChuyen == null) {
            phiVanChuyen = BigDecimal.ZERO;
        }
        if (tienGiam == null) {
            tienGiam = BigDecimal.ZERO;
        }
        if (trangThaiTT == null) {
            trangThaiTT = "CHUA_THANH_TOAN";
        }
        if (trangThaiDon == null) {
            trangThaiDon = "CHO_XAC_NHAN";
        }
        if (phuongThucGiaoHang == null) {
            phuongThucGiaoHang = "SHIP";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
