package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Ghi chú nội bộ của admin về khách hàng.
 * Severity: INFO (xanh lá), WARN (vàng), DANGER (đỏ)
 */
@Entity
@Table(name = "CustomerNotes")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomerNote {

    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARN = "WARN";
    public static final String SEVERITY_DANGER = "DANGER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    /** Mức độ: INFO, WARN, DANGER */
    @Column(nullable = false, length = 20)
    private String severity = SEVERITY_INFO;

    @Column(nullable = false, length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (severity == null) {
            severity = SEVERITY_INFO;
        }
    }
}
