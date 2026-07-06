package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"admin"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminId", nullable = false)
    private User admin;

    @Column(nullable = false, length = 50)
    private String hanhDong;

    @Column(nullable = false, length = 50)
    private String loaiEntity;

    @Column(nullable = false)
    private Integer entityId;

    @Column(columnDefinition = "nvarchar(max)")
    private String giaTriCu;

    @Column(columnDefinition = "nvarchar(max)")
    private String giaTriMoi;

    @Column(columnDefinition = "nvarchar(max)")
    private String moTa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(length = 50)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
