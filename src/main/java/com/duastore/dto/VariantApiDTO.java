package com.duastore.dto;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
 
/**
 * DTO trả về JSON khi khách hàng chọn biến thể sản phẩm.
 *
 * Endpoint: GET /api/variants/{variantId}
 * JavaScript dùng dữ liệu này để:
 *   - Cập nhật ảnh sản phẩm (hinhAnh)
 *   - Cập nhật giá hiển thị (giaGoc / giaKhuyenMai)
 *   - Kiểm tra tồn kho → disable nút "Thêm vào giỏ" nếu hết
 *
 * Ví dụ JSON response:
 * {
 *   "id": 3,
 *   "tenBienThe": "250ml - Nắp Gỗ",
 *   "giaGoc": 38000,
 *   "giaKhuyenMai": 34000,
 *   "soLuongTon": 30,
 *   "hinhAnh": "/uploads/chai-tron-250ml-nap-go.jpg",
 *   "conHang": true
 * }
 *
 * @author anhpl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantApiDTO {
 
    private Integer id;
    private String tenBienThe;
 
    /** Giá gốc (để gạch ngang khi có KM) */
    private BigDecimal giaGoc;
 
    /** Giá khuyến mãi (null = không có KM → hiển thị giaGoc) */
    private BigDecimal giaKhuyenMai;
 
    /** Tồn kho thực tế */
    private Integer soLuongTon;
 
    /**
     * Đường dẫn ảnh biến thể.
     * null → JavaScript giữ nguyên ảnh hiện tại (hinhAnhChinh của Product)
     */
    private String hinhAnh;
 
    /** true nếu soLuongTon > 0 — dùng để JS disable nút thêm giỏ */
    private boolean conHang;
 
    // ── Computed helper ──────────────────────────────────────
 
    /**
     * Giá bán thực tế = giaKhuyenMai nếu có, ngược lại = giaGoc
     */
    public BigDecimal getGiaBan() {
        return giaKhuyenMai != null ? giaKhuyenMai : giaGoc;
    }
}