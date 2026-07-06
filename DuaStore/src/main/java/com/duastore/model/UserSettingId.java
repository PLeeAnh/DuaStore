package com.duastore.model;

import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingId implements Serializable {

    private Integer userId;
    private String settingKey;
}
