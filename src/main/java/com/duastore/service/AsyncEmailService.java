package com.duastore.service;

import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import com.duastore.util.PriceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gui email BAT DONG BO (qua executor "duastoreMailExecutor").
 *
 * TAI SAO CAN:
 *  - SMTP truoc day chay dong bo ngay trong request -> neu SMTP cham/loi, checkout hoac
 *    cap nhat trang thai don se bi chet nghe hang giay den hang phut ("ung dung bi lag").
 *  - Gio day handle tra ve ngay, email chay o thread rieng. Email khong bao gio duoc
 *    dung lam "dieu kien bat buoc" de hoan tat giao dich — chi la notify phu (best-effort).
 *
 * Tat ca method gui email truyen thong van nam trong EmailService (dong bo, dung cho
 * cac truong hop can biet ket qua ngay — vi du sendTest). Lop nay la facade bat dong bo
 * de cac controller/service goi den khi khong muon chet nghe request.
 */
@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý email.
 */
public class AsyncEmailService {

    private static final Logger log = LoggerFactory.getLogger(AsyncEmailService.class);

    private final EmailService emailService;

    public AsyncEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async("duastoreMailExecutor")
    public void sendOrderSuccess(Order order) {
        try {
            if (order == null || order.getUser() == null) {
                return;
            }
            String tt = switch (order.getPhuongThucTT() == null ? "" : order.getPhuongThucTT()) {
                case "CHUYEN_KHOAN" -> "Chuyển khoản";
                case "VNPAY" -> "VNPay";
                default -> "COD";
            };
            String gh = "GHN".equals(order.getShippingCarrier()) ? "Giao Hàng Nhanh" : "Giao Hàng Tiết Kiệm";
            String ngayDat = order.getNgayDat() != null
                    ? order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";

            List<OrderItem> items = order.getOrderItems();
            StringBuilder itemsHtml = new StringBuilder();
            if (items != null) {
                for (OrderItem item : items) {
                    itemsHtml.append("<div style=\"display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid #f0f0f0;\">")
                            .append("<div><div style=\"font-size:14px;color:#424242;\">").append(item.getTenSanPham()).append("</div>")
                            .append("<div style=\"font-size:12px;color:#9e9e9e;\">").append(item.getTenBienThe()).append(" x ").append(item.getSoLuong()).append("</div></div>")
                            .append("<div style=\"font-size:14px;font-weight:600;color:#424242;\">").append(PriceUtils.format(item.getThanhTien())).append("</div></div>");
                }
            }

            emailService.sendOrderSuccessEmail(order.getUser().getEmail(), order.getUser().getHoTen(),
                    order.getMaDon(), ngayDat, order.getSnapDiaChi(), tt, gh,
                    PriceUtils.format(order.getTongThanhToan()), itemsHtml.toString());
        } catch (Exception e) {
            log.warn("Async send order email that bai (maDon={}): {}", order != null ? order.getMaDon() : "-", e.getMessage());
        }
    }

    @Async("duastoreMailExecutor")
    public void sendOrderStatus(String toEmail, String hoTen, String maDon, String trangThaiLabel) {
        try {
            emailService.sendOrderStatusEmail(toEmail, hoTen, maDon, trangThaiLabel);
        } catch (Exception e) {
            log.warn("Async send status email that bai ({}): {}", maDon, e.getMessage());
        }
    }

    @Async("duastoreMailExecutor")
    public void sendOrderAssigned(String toEmail, String adminName, String maDon, String customerName, String assignedBy) {
        try {
            emailService.sendOrderAssignedEmail(toEmail, adminName, maDon, customerName, assignedBy);
        } catch (Exception e) {
            log.warn("Async send assigned email that bai ({}): {}", maDon, e.getMessage());
        }
    }

    @Async("duastoreMailExecutor")
    public void sendPasswordResetSuccess(String toEmail) {
        try {
            emailService.sendPasswordResetSuccess(toEmail);
        } catch (Exception e) {
            log.warn("Async send reset email that bai: {}", e.getMessage());
        }
    }

    @Async("duastoreMailExecutor")
    public void sendRaw(String toEmail, String subject, String htmlContent) {
        try {
            emailService.send(toEmail, subject, htmlContent);
        } catch (Exception e) {
            log.warn("Async send raw email that bai (to={}): {}", toEmail, e.getMessage());
        }
    }
}