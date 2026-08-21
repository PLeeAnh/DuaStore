package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserSettingId.class)
/**
 * Entity ánh xạ dữ liệu người dùng.
 */
public class UserSetting {

    @Id
    @Column(nullable = false)
    private Integer userId;

    @Id
    @Column(nullable = false, length = 50)
    private String settingKey;

    @Column(length = 500)
    private String settingValue;
}
