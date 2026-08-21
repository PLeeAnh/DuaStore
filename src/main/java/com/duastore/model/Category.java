package com.duastore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString(exclude = {"parent", "children"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Categories")
/**
 * Entity ánh xạ dữ liệu danh mục.
 */
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private String tenDanhMuc;

    private String moTa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @SQLRestriction("isActive = 1")
    @JsonIgnore
    private List<Category> children;

    private Integer thuTuHienThi = 0;
    private boolean isActive = true;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String slug;

    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
