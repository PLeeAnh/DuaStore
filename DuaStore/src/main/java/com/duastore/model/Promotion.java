package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String maCode;

    @Column(nullable = false, length = 200)
    private String tenChuongTrinh;

    @Column(nullable = false, length = 15)
    private String loaiGiam;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal donHangToiThieu;

    @Column(precision = 12, scale = 0)
    private BigDecimal giamToiDa;

    @Column
    private Integer soLanDung;

    @Column(nullable = false)
    private Integer daDung;

    @Column(nullable = false)
    private LocalDateTime tuNgay;

    @Column(nullable = false)
    private LocalDateTime denNgay;

    @Column(nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        if (donHangToiThieu == null) donHangToiThieu = BigDecimal.ZERO;
        if (daDung == null) daDung = 0;
        if (isActive == null) isActive = true;
    }
}
