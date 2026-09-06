package com.duastore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_lock_requests")
@Getter
@Setter
@NoArgsConstructor
/**
 * Yêu cầu khóa tài khoản khách hàng do ADMIN/STAFF tạo ra khi khóa một tài khoản
 * không bị nghi ngờ là bot — cần PRODUCT_OWNER duyệt (đồng ý/từ chối) thì mới thực
 * sự có hiệu lực.
 */
public class AccountLockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer requestedBy;

    private Integer decidedBy;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(length = 500)
    private String decisionNote;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }
}
