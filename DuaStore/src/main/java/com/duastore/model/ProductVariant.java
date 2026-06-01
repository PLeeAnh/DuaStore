/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * ★ ProductVariant — Entity Biến thể sản phẩm
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Entity đại diện cho bảng `product_variants` trong database.
 * Mỗi biến thể là một tùy chọn của sản phẩm (size, màu sắc, giá riêng, tồn kho).
 * 
 * Ví dụ: Sản phẩm "Áo thun nam" có thể có biến thể:
 *   - Size M, màu Đen, giá 200.000đ, tồn 50
 *   - Size L, màu Đen, giá 200.000đ, tồn 30
 *   - Size M, màu Trắng, giá 220.000đ, tồn 20
 * 
 * ★ TODO [Nguyễn Văn A]: Định nghĩa các thuộc tính
 *   - Long id (@Id, @GeneratedValue) — Khóa chính
 *   - Product product (@ManyToOne(fetch = FetchType.LAZY), @JoinColumn(name = "product_id"))
 *     — Sản phẩm cha
 *   - String size (@Column) — Kích thước (S, M, L, XL, v.v.)
 *   - String color (@Column) — Màu sắc
 *   - BigDecimal price (@Column(precision = 12, scale = 2)) — Giá riêng (nếu null thì dùng basePrice)
 *   - Integer stock (@Column(nullable = false, columnDefinition = "int default 0")) — Số lượng tồn kho
 *   - String image (@Column) — Ảnh riêng cho biến thể (nếu có)
 *   - Integer status (@Column(nullable = false, columnDefinition = "int default 1")) — 1: active, 0: inactive
 * 
 * ★ TODO [Nguyễn Văn A]: Annotation bổ sung
 *   - @Table(name = "product_variants", uniqueConstraints =
 *     @UniqueConstraint(columnNames = {"product_id", "size", "color"}))
 *     — Đảm bảo không trùng size + color trong cùng product
 * 
 * ⚠ Lưu ý:
 *   - Sử dụng jakarta.persistence (Spring Boot 3.x)
 *   - Nên dùng Lombok @Data, @NoArgsConstructor, @AllArgsConstructor
 *   - Cần unique constraint (product_id, size, color) để tránh trùng lặp
 *   - stock = 0 hoặc null nghĩa là hết hàng
 *   - price có thể null → khi đó dùng basePrice từ Product
 */
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column
    private String size;
    
    @Column
    private String color;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer stock;
    
    @Column
    private String image;
    
    @Column(nullable = false, columnDefinition = "int default 1")
    private Integer status;
    
}
