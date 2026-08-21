package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"order"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu đơn hàng.
 */
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Column
    private Integer productId;

    @Column
    private Integer variantId;

    @Column(nullable = false, length = 200)
    private String tenSanPham;

    @Column(length = 150)
    private String tenBienThe;

    @Column(length = 255)
    private String hinhAnhSP;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal donGia;

    @Column(nullable = false)
    private Integer soLuong;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal thanhTien;

    @Column(length = 20)
    private String loaiGia;
}
