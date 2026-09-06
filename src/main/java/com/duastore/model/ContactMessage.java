package com.duastore.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu liên hệ.
 */
public class ContactMessage {

    // Các mã phân loại tự động
    public static final String LOAI_DON_HANG = "DON_HANG";
    public static final String LOAI_SAN_PHAM = "SAN_PHAM";
    public static final String LOAI_GIAO_HANG = "GIAO_HANG";
    public static final String LOAI_THANH_TOAN = "THANH_TOAN";
    public static final String LOAI_KHIEU_NAI = "KHIEU_NAI";
    public static final String LOAI_HOP_TAC = "HOP_TAC";
    public static final String LOAI_KHAC = "KHAC";
    public static final String LOAI_RAC = "RAC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, length = 150)
    private String hoTen;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 2000)
    private String noiDung;

    @Column(name = "phan_loai", nullable = false, length = 30)
    private String phanLoai = LOAI_KHAC;

    @Column(name = "is_spam", nullable = false)
    private Boolean isSpam = false;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_resolved", nullable = false)
    private Boolean isResolved = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isSpam == null) {
            isSpam = false;
        }
        if (isRead == null) {
            isRead = false;
        }
        if (isResolved == null) {
            isResolved = false;
        }
    }

    public String getPhanLoaiKey() {
        if (Boolean.TRUE.equals(isSpam)) {
            return LOAI_RAC;
        }
        return phanLoai;
    }
}