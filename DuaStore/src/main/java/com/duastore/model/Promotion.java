package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VoucherType voucherType = VoucherType.VOUCHER;

    private Integer priority = 0;

    private Boolean stackable = false;

    @Column(precision = 12, scale = 0)
    private BigDecimal budget;

    @Column(precision = 12, scale = 0)
    private BigDecimal usedBudget;

    private Integer maxClaims;

    private Integer maxClaimsPerUser;

    @Column(length = 20)
    private String targetType;

    @Column(length = 500)
    private String targetIds;

    private Integer savedCount = 0;

    @PrePersist
    protected void onCreate() {
        normalizeDefaults();
    }

    @PostLoad
    protected void normalizeDefaults() {
        if (donHangToiThieu == null) {
            donHangToiThieu = BigDecimal.ZERO;
        }
        if (daDung == null) {
            daDung = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (usedBudget == null) {
            usedBudget = BigDecimal.ZERO;
        }
        if (voucherType == null) {
            voucherType = VoucherType.VOUCHER;
        }
        if (priority == null) {
            priority = 0;
        }
        if (stackable == null) {
            stackable = false;
        }
        if (savedCount == null) {
            savedCount = 0;
        }
    }
}
