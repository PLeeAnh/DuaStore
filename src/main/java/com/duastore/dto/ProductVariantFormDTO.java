package com.duastore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO dùng cho form Thêm / Sửa biến thể sản phẩm (Admin) Mỗi biến thể = 1 SKU:
 * dung tích + kiểu nắp + giá + tồn kho + ảnh riêng
 *
 * @author anhpl
 */
@Data
@NoArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu sản phẩm, biến thể sản phẩm giữa các tầng controller/service/view.
 */
public class ProductVariantFormDTO {

    /**
     * null = thêm mới · có giá trị = sửa
     */
    private Integer id;

    /**
     * ID sản phẩm gốc (cha)
     */
    @NotNull(message = "Thiếu thông tin sản phẩm cha")
    private Integer productId;

    /**
     * VD: "250ml - Nắp Gỗ"
     */
    @NotBlank(message = "Tên biến thể không được để trống")
    private String tenBienThe;

    /**
     * Dung tích tính bằng ml (null nếu phân loại theo màu/chiều cao)
     */
    private Integer dungTich;

    /**
     * Giá gốc trước khuyến mãi
     */
    @NotNull(message = "Vui lòng nhập giá gốc")
    @DecimalMin(value = "0", inclusive = false, message = "Giá gốc phải lớn hơn 0")
    private BigDecimal giaGoc;

    /**
     * Giá khuyến mãi (null = không có KM) Nên nhỏ hơn giaGoc — validate trong
     * Service
     */
    private BigDecimal giaKhuyenMai;

    /**
     * Số lượng tồn kho hiện tại
     */
    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer soLuongTon = 0;

    private BigDecimal giaVon;

    private Integer lowStockThreshold = 20;

    /**
     * Đường dẫn ảnh hiện tại (khi sửa)
     */
    private String hinhAnh;

    /**
     * File ảnh mới upload cho biến thể này
     */
    private MultipartFile hinhAnhFile;

    /**
     * true = biến thể này được hiển thị mặc định khi vào trang chi tiết SP Chỉ
     * 1 biến thể/sản phẩm được isDefault = true
     */
    private boolean isDefault = false;
}
