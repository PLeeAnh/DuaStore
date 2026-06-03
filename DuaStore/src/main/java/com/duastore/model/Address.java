package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String tenNguoiNhan;

    @Column(nullable = false, length = 15)
    private String soDienThoai;

    @Column(nullable = false, length = 100)
    private String tinhThanh;

    @Column(nullable = false, length = 100)
    private String quanHuyen;

    @Column(nullable = false, length = 100)
    private String phuongXa;

    @Column(nullable = false, length = 200)
    private String diaChiCuThe;

    @Column(nullable = false)
    private Boolean isDefault;

    @PrePersist
    protected void onCreate() {
        if (isDefault == null) isDefault = false;
    }

    public String getDiaChiDayDu() {
        return diaChiCuThe + ", " + phuongXa + ", " + quanHuyen + ", " + tinhThanh;
    }
}
