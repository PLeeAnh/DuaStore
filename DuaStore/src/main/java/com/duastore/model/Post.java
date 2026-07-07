package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, length = 300)
    private String tieuDe;

    @Column(length = 500)
    private String slug;

    @Column(length = 500)
    private String metaDescription;

    @Column(length = 500)
    private String tomTat;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(length = 255)
    private String hinhAnh;

    private Integer tacGiaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "danhMucId")
    private PostCategory danhMuc;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "Post_Tags",
            joinColumns = @JoinColumn(name = "postId"),
            inverseJoinColumns = @JoinColumn(name = "tagId"))
    private Set<PostTag> tags = new HashSet<>();

    @Column(nullable = false, length = 15)
    private String trangThai = "NHAP";

    @Column(nullable = false)
    private Integer luotXem = 0;

    @Column(name = "isFeatured")
    private Boolean featured = false;

    private LocalDateTime ngayXuatBan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = "NHAP";
        }
        if (luotXem == null) {
            luotXem = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
