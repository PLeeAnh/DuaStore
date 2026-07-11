package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private Boolean isApproved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewId")
    @jakarta.persistence.OrderBy("sortOrder ASC")
    private List<ReviewImage> images = new ArrayList<>();

    @Transient
    public String getHinhAnh() {
        return (images != null && !images.isEmpty()) ? images.get(0).getImageUrl() : null;
    }

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        if (isApproved == null) {
            isApproved = false;
        }
    }
}
