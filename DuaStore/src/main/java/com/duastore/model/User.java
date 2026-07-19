package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String hoTen;

    @Column(length = 15)
    private String soDienThoai;

    @Column(length = 255)
    private String avatar;

    @Column(length = 100)
    private String nickname;

    @Column(length = 20)
    private String status = "ONLINE";

    @Column
    private Boolean emailVisible = false;

    @Column
    private Boolean phoneVisible = false;

    @Column
    private Boolean emailMarketing = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column
    private LocalDateTime ngayCapNhat;

    @Column
    private LocalDate ngaySinh;

    @Column(length = 64)
    private String twoFactorSecret;

    @Column
    private Boolean twoFactorEnabled = false;

    @Column(length = 255)
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (twoFactorEnabled == null) {
            twoFactorEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
