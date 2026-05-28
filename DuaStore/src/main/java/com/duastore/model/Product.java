/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.duastore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ★ Product — Entity Sản phẩm
 * 
 * ========== LUỒNG / HƯỚNG DẪN ==========
 * Entity đại diện cho bảng `products` trong database.
 * Sử dụng JPA annotations để mapping với database.
 * 
 * Mỗi sản phẩm có thể có nhiều ProductVariant (size, màu sắc).
 * Ảnh sản phẩm lưu dưới dạng chuỗi JSON array hoặc chỉ lưu ảnh chính.
 * 
 * ★ TODO [Nguyễn Văn A]: Định nghĩa các thuộc tính
 *   - Long id (@Id, @GeneratedValue(strategy = GenerationType.IDENTITY))
 *   - String name (@Column(nullable = false)) — Tên sản phẩm
 *   - String slug (@Column(unique = true)) — URL thân thiện, tự sinh từ name
 *   - String description (@Column(columnDefinition = "TEXT")) — Mô tả chi tiết
 *   - BigDecimal basePrice (@Column(nullable = false, precision = 12, scale = 2)) — Giá gốc
 *   - String images (@Column(columnDefinition = "TEXT")) — JSON array chứa đường dẫn ảnh
 *   - Long categoryId (@Column) — FK đến bảng categories (hoặc dùng @ManyToOne)
 *   - Integer status (@Column(nullable = false, columnDefinition = "int default 1")) — 1: active, 0: inactive
 *   - LocalDateTime createdAt (@Column(updatable = false)) — Thời gian tạo
 *   - LocalDateTime updatedAt — Thời gian cập nhật
 * 
 * ★ TODO [Nguyễn Văn A]: Quan hệ JPA
 *   - @ManyToOne(fetch = FetchType.LAZY) với Category (nếu có entity Category)
 *   - @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
 *     với List<ProductVariant>
 * 
 * ★ TODO [Nguyễn Văn A]: @PrePersist / @PreUpdate
 *   - Tự động set createdAt và updatedAt
 *   - Tự động sinh slug từ name (removeUnicode, replace spaces with hyphens)
 * 
 * ⚠ Lưu ý:
 *   - Sử dụng jakarta.persistence (Spring Boot 3.x)
 *   - images lưu dưới dạng JSON string, parse bằng Jackson khi cần
 *   - Nên sử dụng Lombok @Data, @NoArgsConstructor, @AllArgsConstructor
 *   - Slug cần unique để SEO
 */
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    private String slug;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;
    
    @Column(columnDefinition = "TEXT")
    private String images;
    
    @Column
    private Long categoryId;
    
    @Column(nullable = false, columnDefinition = "int default 1")
    private Integer status;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
}
