package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu thông báo.
 */
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(length = 20)
    private String linkType;

    private Integer linkId;

    @Column(length = 500)
    private String linkUrl;

    @Column(length = 255)
    private String linkLabel;

    private Integer userId;

    @Column(length = 20)
    private String targetRole;

    /**
     * Quyen can co de THAY thong bao nay (vd "ORDER_READ", "PROMOTION_READ" — xem
     * PermissionEnum). null = ai trong nhom targetRole cung thay (thong bao chung).
     * Loc theo dung nghiep vu cua tung vai tro, thay vi moi ADMIN/STAFF/PRODUCT_OWNER
     * deu thay CHUNG mot dong thong bao du khong lien quan den cong viec cua ho.
     */
    @Column(length = 40)
    private String requiredPermission;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
