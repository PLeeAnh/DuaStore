package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer danhGia;

    @Column(length = 1000)
    private String binhLuan;

    @Column(length = 500)
    private String hinhAnh;

    @Column(nullable = false)
    private Boolean isApproved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        if (isApproved == null) isApproved = false;
    }
}
