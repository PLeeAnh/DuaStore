package com.duastore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AdminNotificationDTO {

    private Integer id;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private Integer productId;
    private Integer promotionId;
}
