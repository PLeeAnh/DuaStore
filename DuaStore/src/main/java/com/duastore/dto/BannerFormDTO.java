package com.duastore.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class BannerFormDTO {

    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private String imageUrl;
    private MultipartFile imageFile;

    @Size(max = 1000, message = "Link không được vượt quá 1000 ký tự")
    @Pattern(regexp = "^$|^(https?://|/)[^\\s]*$", message = "Link phải là URL http(s) hoặc đường dẫn bắt đầu bằng /")
    private String linkUrl;

    private Boolean active = true;

    @Min(value = 0, message = "Thứ tự hiển thị phải từ 0 trở lên")
    @Max(value = 100000, message = "Thứ tự hiển thị không hợp lệ")
    private Integer displayOrder = 0;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDate;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @AssertTrue(message = "Thời gian kết thúc phải sau thời gian bắt đầu")
    public boolean isDisplayPeriodValid() {
        return startDate == null || endDate == null || endDate.isAfter(startDate);
    }
}
