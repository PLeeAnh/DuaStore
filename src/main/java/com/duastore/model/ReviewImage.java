package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ReviewImages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu đánh giá sản phẩm.
 */
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private Integer reviewId;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
