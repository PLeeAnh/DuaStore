package com.duastore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RefundRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * Entity ánh xạ dữ liệu hoàn trả/đổi trả đơn hàng.
 */
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Integer userId;

    @Column(name = "lydo", nullable = false, length = 2000)
    private String lydo;

    @Column(name = "soTienHoan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "trangThai", nullable = false)
    private String trangThai = "CHO_DUYET";

    @Column(name = "anhMinhChung")
    private String anhMinhChung;

    @Column(name = "phuongThucHoan")
    private String phuongThucHoan;

    @Column(name = "tenNganHang")
    private String tenNganHang;

    @Column(name = "soTaiKhoan")
    private String soTaiKhoan;

    @Column(name = "chuTaiKhoan")
    private String chuTaiKhoan;

    @Column(nullable = false, updatable = false)
    private LocalDateTime ngayYeuCau;

    @Column
    private LocalDateTime ngayXuLy;

    @Column
    private Integer nguoiXuLyId;

    @Column(name = "ghiChuXuLy")
    private String ghiChuXuLy;

    // Glass-specific fields
    @Column(name = "loaiYeuCau", length = 30)
    private String loaiYeuCau = "HOAN_TIEN"; // HOAN_TIEN, DOI_SIZE, DOI_MAU, DOI_SAN_PHAM_KHAC

    @Column(name = "lyDoChiTiet", length = 50)
    private String lyDoChiTiet; // LOI_HANG, KHONG_DUNG_MO_TA, DOI_Y, KHAC

    @Column(name = "trangThaiDonHangKhiYeuCau", length = 30)
    private String trangThaiDonHangKhiYeuCau;

    @Column(name = "daKiemTraHang")
    private Boolean daKiemTraHang = false;

    @Column(name = "tinhTrangHangTra", length = 30)
    private String tinhTrangHangTra; // NGUYEN_VINH, VO_VANG, THIEU_PHU_KIEN, CHUA_NHAN

    @Column(name = "phiShipTraLai", precision = 18, scale = 2)
    private BigDecimal phiShipTraLai = BigDecimal.ZERO;

    @Column(name = "maVanDonTra", length = 100)
    private String maVanDonTra;

    @Column(name = "ngayNhanHangTra")
    private LocalDateTime ngayNhanHangTra;

    @Column(name = "phuongThucHoanTien", length = 30)
    private String phuongThucHoanTien; // CHUYEN_KHOAN, VNPAY_REFUND, TIEN_MAT

    @Column(name = "soTienThucTeHoan", precision = 18, scale = 2)
    private BigDecimal soTienThucTeHoan;

    @Column(name = "videoUnboxing", length = 500)
    private String videoUnboxing;

    @Column(name = "variantMoiId")
    private Integer variantMoiId;

    @Column(name = "anhThucTe", length = 500)
    private String anhThucTe;

    @PrePersist
    protected void onCreate() {
        ngayYeuCau = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = "CHO_DUYET";
        }
        if (loaiYeuCau == null) {
            loaiYeuCau = "HOAN_TIEN";
        }
        if (phiShipTraLai == null) {
            phiShipTraLai = BigDecimal.ZERO;
        }
        if (daKiemTraHang == null) {
            daKiemTraHang = false;
        }
    }
}