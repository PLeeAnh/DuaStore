package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "nguoiThucHien"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_su_kien", nullable = false, length = 50)
    private OrderEventType loaiSuKien;

    @Column(name = "trang_thai_cu", length = 50)
    private String trangThaiCu;

    @Column(name = "trang_thai_moi", length = 50)
    private String trangThaiMoi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private User nguoiThucHien;

    @Column(length = 500)
    private String ghiChu;

    @Column(name = "thoi_gian", nullable = false, updatable = false)
    private LocalDateTime thoiGian;

    @PrePersist
    protected void onCreate() {
        thoiGian = LocalDateTime.now();
    }
}
