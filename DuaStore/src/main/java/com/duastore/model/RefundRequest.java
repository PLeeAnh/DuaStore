package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RefundRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Integer userId;

    @Column(name = "lydo", nullable = false, length = 2000)
    private String lydo;

    @Column(name = "soTienHoan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "trangThai", nullable = false)
    private String trangThai = "CHO_DUYET";

    @Column(name = "anhMinhChung")
    private String anhMinhChung;

    @Column(name = "phuongThucHoan")
    private String phuongThucHoan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayYeuCau;

    @Column
    private LocalDateTime ngayXuLy;

    @Column
    private Integer nguoiXuLyId;

    @Column(name = "ghiChuXuLy")
    private String ghiChuXuLy;

    @PrePersist
    protected void onCreate() {
        ngayYeuCau = LocalDateTime.now();
        if (trangThai == null) trangThai = "CHO_DUYET";
    }
}
