package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Wishlists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "productId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity ánh xạ dữ liệu danh sách yêu thích.
 */
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer productId;

    @Column(updatable = false)
    private LocalDateTime ngayThem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", insertable = false, updatable = false)
    private Product product;

    @PrePersist
    protected void onCreate() {
        if (ngayThem == null) {
            ngayThem = LocalDateTime.now();
        }
    }
}
