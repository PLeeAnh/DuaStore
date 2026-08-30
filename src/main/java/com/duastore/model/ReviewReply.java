package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Phản hồi của admin cho một đánh giá sản phẩm.
 */
@Entity
@Table(name = "ReviewReplies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReviewReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Đánh giá được phản hồi */
    @Column(nullable = false)
    private Integer reviewId;

    /** Nội dung phản hồi */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    /** Admin đã viết phản hồi */
    @Column(nullable = false)
    private Integer createdBy;

    /** Thời gian tạo */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
