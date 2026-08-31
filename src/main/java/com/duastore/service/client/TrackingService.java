package com.duastore.service.client;

import com.duastore.dto.TrackingDataDTO;
import com.duastore.model.Order;
import com.duastore.repository.AddressRepository;
import com.duastore.service.GHNShippingService;
import com.duastore.service.ShippingFeeService;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý theo dõi trạng thái đơn hàng/vận đơn.
 */
public class TrackingService {

    private final OrderService orderService;
    private final GHNShippingService ghnShippingService;
    private final AddressRepository addressRepository;
    private final double storeLat;
    private final double storeLng;

    public TrackingService(OrderService orderService,
            GHNShippingService ghnShippingService,
            AddressRepository addressRepository,
            ShippingFeeService shippingFeeService) {
        this.orderService = orderService;
        this.ghnShippingService = ghnShippingService;
        this.addressRepository = addressRepository;
        this.storeLat = shippingFeeService.getStoreLat();
        this.storeLng = shippingFeeService.getStoreLng();
    }

    public TrackingDataDTO getTrackingData(String maDon) {
        Order order = orderService.getOrderByMaDon(maDon);
        TrackingDataDTO dto = new TrackingDataDTO();
        dto.setMaDon(order.getMaDon());
        dto.setTrangThaiDon(order.getTrangThaiDon());
        dto.setTrangThaiDonDisplay(statusDisplay(order.getTrangThaiDon()));
        dto.setShippingCarrier(order.getShippingCarrier());
        dto.setCarrierName(carrierName(order.getShippingCarrier()));
        dto.setMaVanDon(order.getMaVanDon());
        dto.setCarrierTrackingUrl(getCarrierUrl(order.getMaVanDon(), order.getShippingCarrier()));
        dto.setStoreLat(storeLat);
        dto.setStoreLng(storeLng);

        if (order.getAddressId() != null) {
            addressRepository.findById(order.getAddressId()).ifPresent(addr -> {
                dto.setCustomerLat(addr.getLatitude());
                dto.setCustomerLng(addr.getLongitude());
            });
        }
        if (dto.getCustomerLat() == null) {
            dto.setCustomerLat(storeLat);
            dto.setCustomerLng(storeLng);
        }
        dto.setCustomerAddress(order.getSnapDiaChi());
        dto.setTimeline(orderService.getOrderTimeline(maDon));
        dto.setSnapTenNguoiNhan(order.getSnapTenNguoiNhan());
        dto.setSnapSoDienThoai(order.getSnapSoDienThoai());
        dto.setSnapDiaChi(order.getSnapDiaChi());
        dto.setTongThanhToan(order.getTongThanhToan());
        dto.setPhuongThucTT(order.getPhuongThucTT());
        dto.setPhuongThucTTDisplay(paymentDisplay(order.getPhuongThucTT()));
        dto.setNgayDat(order.getNgayDat() != null ? order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        dto.setGhiChu(order.getGhiChu());
        dto.setVerified(true);
        return dto;
    }

    public String getCarrierUrl(String maVanDon, String carrier) {
        if (maVanDon == null || maVanDon.isBlank()) return null;
        return switch (carrier != null ? carrier : "") {
            case "GHN" -> "https://donhang.ghn.vn/?order_code=" + maVanDon;
            case "GHTK" -> "https://khachhang.giaohangtietkiem.vn/tracking?code=" + maVanDon;
            default -> null;
        };
    }

    public Map<String, Object> pollCarrierStatus(String maDon) {
        Order order = orderService.getOrderByMaDon(maDon);
        if (order.getMaVanDon() == null || order.getMaVanDon().isBlank()) return null;
        if (!"GHN".equals(order.getShippingCarrier())) return null;
        return ghnShippingService.getOrderDetail(order.getMaVanDon());
    }

    public boolean verifyOrder(String maDon, String phone) {
        try {
            Order order = orderService.getOrderByMaDon(maDon);
            if (phone == null || phone.isBlank()) return false;
            String storedPhone = order.getSnapSoDienThoai();
            if (storedPhone == null) return false;
            return storedPhone.contains(phone.replaceAll("[^0-9]", ""))
                    || phone.replaceAll("[^0-9]", "").contains(storedPhone.replaceAll("[^0-9]", ""));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private String statusDisplay(String status) {
        if (status == null) return "";
        return switch (status) {
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DA_XAC_NHAN" -> "Đã xác nhận";
            case "DANG_GIAO" -> "Đang giao hàng";
            case "DA_GIAO" -> "Đã giao hàng";
            case "DA_HOAN_THANH" -> "Hoàn thành";
            case "DA_HUY" -> "Đã hủy";
            case "DA_HOAN_TIEN" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    private String carrierName(String carrier) {
        if (carrier == null) return "";
        return "GHN".equals(carrier) ? "Giao Hàng Nhanh" : "Giao Hàng Tiết Kiệm";
    }

    private String paymentDisplay(String method) {
        if (method == null) return "";
        return switch (method) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "CHUYEN_KHOAN" -> "Chuyển khoản";
            case "SEPAY_QR" -> "QR (VietQR)";
            default -> method;
        };
    }
}
