package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order", "admin"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminId", nullable = false)
    private User admin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayPhan;

    @Column(length = 20)
    private String trangThai;

    @PrePersist
    protected void onCreate() {
        ngayPhan = LocalDateTime.now();
        if (trangThai == null) trangThai = "DANG_XU_LY";
    }
}
