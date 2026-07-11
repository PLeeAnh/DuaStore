package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PriceHistory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer variantId;

    private String variantName;

    private Integer productId;

    private String productName;

    @Column(precision = 18, scale = 2)
    private BigDecimal giaCu;

    @Column(precision = 18, scale = 2)
    private BigDecimal giaMoi;

    private Integer nguoiThayDoiId;

    @Column(nullable = false)
    private LocalDateTime ngayThayDoi;

    private String nguon;

    @PrePersist
    protected void onCreate() {
        ngayThayDoi = LocalDateTime.now();
    }
}
