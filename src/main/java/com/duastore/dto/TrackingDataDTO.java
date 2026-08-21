package com.duastore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * DTO (Data Transfer Object) dùng để truyền dữ liệu theo dõi trạng thái đơn hàng/vận đơn giữa các tầng controller/service/view.
 */
public class TrackingDataDTO {
    private String maDon;
    private String trangThaiDon;
    private String trangThaiDonDisplay;
    private String shippingCarrier;
    private String carrierName;
    private String maVanDon;
    private String carrierTrackingUrl;
    private Double storeLat;
    private Double storeLng;
    private Double customerLat;
    private Double customerLng;
    private String customerAddress;
    private List<TimelineEvent> timeline;
    private String snapTenNguoiNhan;
    private String snapSoDienThoai;
    private String snapDiaChi;
    private BigDecimal tongThanhToan;
    private String phuongThucTT;
    private String phuongThucTTDisplay;
    private String ngayDat;
    private String ghiChu;
    private boolean verified;
}
