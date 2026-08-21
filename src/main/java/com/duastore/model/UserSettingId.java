package com.duastore.model;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * Lớp hỗ trợ xử lý người dùng.
 */
public class UserSettingId implements Serializable {

    private Integer userId;
    private String settingKey;
}
