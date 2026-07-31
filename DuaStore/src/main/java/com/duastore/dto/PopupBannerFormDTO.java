package com.duastore.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class PopupBannerFormDTO {

    private Integer id;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    private String imageUrl;
    private MultipartFile imageFile;

    @Size(max = 1000, message = "Link không được vượt quá 1000 ký tự")
    @Pattern(regexp = "^$|^(https?://|/)[^\\s]*$", message = "Link phải là URL http(s) hoặc đường dẫn bắt đầu bằng /")
    private String linkUrl;

    @NotBlank(message = "Vui lòng chọn chế độ hiển thị")
    @Pattern(regexp = "ONCE|EVERY_VISIT|INTERVAL", message = "Chế độ hiển thị không hợp lệ")
    private String displayMode = "EVERY_VISIT";

    @Min(value = 1, message = "Thời gian lặp lại phải từ 1 phút trở lên")
    @Max(value = 10080, message = "Thời gian lặp lại không được vượt quá 7 ngày (10080 phút)")
    private Integer intervalMinutes;

    private Boolean active = true;
}
