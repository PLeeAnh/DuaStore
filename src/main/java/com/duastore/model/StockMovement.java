package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Ghi nhận mỗi lần thay đổi tồn kho biến thể sản phẩm.
 * Types: IN (nhập hàng), OUT (bán/huỷ), ADJUST (điều chỉnh thủ công)
 */
@Entity
@Table(name = "StockMovements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    /** Biến thể sản phẩm bị thay đổi tồn kho */
    @Column(nullable = false)
    private Integer variantId;

    /** Số lượng thay đổi: dương = nhập, âm = xuất */
    @Column(nullable = false)
    private Integer quantity;

    /** Loại hình thay đổi: IN, OUT, ADJUST */
    @Column(nullable = false, length = 20)
    private String type;

    /** Liên kết đơn hàng (nếu là bán/huỷ) */
    private Integer orderId;

    /** Người thực hiện thao tác */
    @Column(nullable = false)
    private Integer userId;

    /** Ghi chú lý do thay đổi */
    @Column(length = 500)
    private String note;

    /** Tồn kho trước khi thay đổi */
    @Column(nullable = false)
    private Integer stockBefore;

    /** Tồn kho sau khi thay đổi */
    @Column(nullable = false)
    private Integer stockAfter;

    /** Thời gian ghi nhận */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
