package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserVouchers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "promotionId"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotionId", nullable = false)
    private Promotion promotion;

    @Column(length = 50)
    private String voucherCode;

    private Integer remainingUses;

    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;

    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private VoucherStatus status = VoucherStatus.AVAILABLE;

    private BigDecimal totalSaved = BigDecimal.ZERO;

    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
        if (status == null) {
            status = VoucherStatus.AVAILABLE;
        }
        if (totalSaved == null) {
            totalSaved = BigDecimal.ZERO;
        }
    }
}
