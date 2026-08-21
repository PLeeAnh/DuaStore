package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserActivityLogs")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Entity ánh xạ dữ liệu người dùng, nhật ký hoạt động người dùng, nhật ký hệ thống.
 */
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String activityType;

    @Column(length = 500)
    private String description;

    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime activityAt;

    @PrePersist
    protected void onCreate() {
        if (activityAt == null) {
            activityAt = LocalDateTime.now();
        }
    }
}
