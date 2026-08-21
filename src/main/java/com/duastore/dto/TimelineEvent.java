package com.duastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu timeline event giữa các tầng controller/service/view.
 */
public class TimelineEvent {
    private String status;
    private String description;
    private LocalDateTime time;
    private String icon;
    private boolean completed;
    private boolean active;
}
