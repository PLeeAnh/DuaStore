package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PostCategories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String tenDanhMuc;

    @Column(length = 500)
    private String moTa;

    @Column(length = 300)
    private String slug;

    private Integer thuTu = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
