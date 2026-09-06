package com.duastore.service;

import com.duastore.model.Order;
import com.duastore.model.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

@Service
/**
 * Service chứa nghiệp vụ (business logic) xử lý email.
 */
public class EmailService implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender defaultMailSender;
    private final SiteSettingService siteSettingService;

    @Value("${spring.mail.username}")
    private String defaultFromEmail;

    @Value("${app.url}")
    private String appUrl;

    private JavaMailSenderImpl dynamicSender;
    private String lastSettingsHash = "";

    public EmailService(JavaMailSender mailSender, SiteSettingService siteSettingService) {
        this.defaultMailSender = mailSender;
        this.siteSettingService = siteSettingService;
    }

    // Domain gia dung cho du lieu seed/test (users.email vd "admin2@duastore.vn") — khong ton tai
    // ngoai thuc te, gui vao se bi bounce. Loc ra o day de cac luong thong bao hang loat (vd
    // "Don hang moi" gui toi tat ca ADMIN/STAFF) khong lam ngap hop thu that bang email bounce.
    private static final String PLACEHOLDER_EMAIL_DOMAIN = "@duastore.vn";

    public static boolean isPlaceholderEmail(String email) {
        return email == null || email.isBlank() || email.trim().toLowerCase().endsWith(PLACEHOLDER_EMAIL_DOMAIN);
    }

    private JavaMailSender resolveMailSender() {
        String host = siteSettingService.getValue("email_host");
        if (host == null || host.isBlank()) {
            return defaultMailSender;
        }

        String port = siteSettingService.getValue("email_port", "587");
        String username = siteSettingService.getValue("email_username", "");
        String password = siteSettingService.getValue("email_password", "");
        String encryption = siteSettingService.getValue("email_encryption", "tls");
        String hash = host + "|" + port + "|" + username + "|" + encryption;

        if (dynamicSender != null && hash.equals(lastSettingsHash)) {
            return dynamicSender;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(Integer.parseInt(port));
        if (!username.isBlank()) sender.setUsername(username);
        if (!password.isBlank()) sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        if ("ssl".equals(encryption)) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else if ("tls".equals(encryption)) {
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }

        dynamicSender = sender;
        lastSettingsHash = hash;
        return sender;
    }

    private String resolveFromEmail() {
        String from = siteSettingService.getValue("email_from");
        return (from != null && !from.isBlank()) ? from : defaultFromEmail;
    }

    private String resolveFromName() {
        String name = siteSettingService.getValue("email_from_name");
        return (name != null && !name.isBlank()) ? name : "DuaStore";
    }

    private String resolveContactPhone() {
        String phone = siteSettingService.getValue("store_phone");
        return (phone != null && !phone.isBlank()) ? phone : "0901 234 567";
    }

    private static String paymentMethodLabel(String code) {
        if (code == null) {
            return "Thanh toán khi nhận hàng (COD)";
        }
        return switch (code) {
            case "CHUYEN_KHOAN" -> "Chuyển khoản ngân hàng (thủ công)";
            case "SEPAY_QR" -> "QR tự động (SePay)";
            default -> "Thanh toán khi nhận hàng (COD)";
        };
    }

    private static String shippingLabel(String carrier) {
        return "GHN".equals(carrier) ? "Giao Hàng Nhanh" : "Giao Hàng Tiết Kiệm";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(toPlainText(body), body);
            sender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    public boolean sendTest() {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            String from = resolveFromEmail();
            helper.setFrom(from, resolveFromName());
            helper.setTo(from);
            helper.setSubject("[DuaStore] Email kiểm tra cấu hình SMTP");
            String html = """
                <div style="font-family:Arial;padding:20px;">
                  <h2 style="color:#e53935;">DuaStore</h2>
                  <p>Đây là email thử nghiệm được gửi từ trang <strong>Cấu hình Email/SMTP</strong>.</p>
                  <p>Nếu bạn nhận được email này, cấu hình SMTP đã hoạt động bình thường.</p>
                </div>
                """;
            helper.setText(toPlainText(html), html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.error("Test SMTP send failed: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);

            String subject = purpose.equals("REGISTER")
                    ? "[DuaStore] Mã xác thực đăng ký tài khoản"
                    : "[DuaStore] Mã xác thực đặt lại mật khẩu";

            String action = purpose.equals("REGISTER")
                    ? "đăng ký tài khoản" : "đặt lại mật khẩu";

            String html = """
                <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
                  <table width="480" align="center"
                         style="background:#fff;border-radius:16px;overflow:hidden;
                                box-shadow:0 4px 24px rgba(0,0,0,.1);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#e53935,#c62828);
                                 padding:32px;text-align:center;">
                        <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                        <div style="color:rgba(255,255,255,.8);font-size:13px;">&#272;&#7891; Th&#7911;y Tinh Cao C&#7845;p</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="font-size:15px;color:#616161;">
                          B&#7841;n &#273;ã y&#234u c&#7847u <strong>%s</strong> t&#7841;i DuaStore.<br/>
                          Vui l&ograve;ng d&ugrave;ng m&atilde; OTP b&ecirc;n d&#432;&#7899;i:
                        </p>
                        <div style="background:#fff8f8;border:2px dashed #e53935;
                                    border-radius:12px;padding:24px;text-align:center;">
                          <div style="font-size:13px;color:#9e9e9e;letter-spacing:2px;">
                            M&Atilde; X&Aacute;C TH&#7920;C
                          </div>
                          <div style="font-size:44px;font-weight:900;letter-spacing:12px;
                                      color:#e53935;font-family:monospace;">
                            %s
                          </div>
                          <div style="font-size:12px;color:#9e9e9e;margin-top:8px;">
                            Hi&#7879;u l&#7909;c trong <strong>5 ph&uacute;t</strong>
                          </div>
                        </div>
                        <p style="font-size:12px;color:#9e9e9e;margin-top:20px;">
                          Kh&ocirc;ng chia s&#7867; m&atilde; n&agrave;y v&#7899;i b&#7845;t k&#7923; ai.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:16px;text-align:center;
                                 border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">
                          &copy; 2026 DuaStore
                        </span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(action, otp);

            String plainText = "DuaStore - Ma xac thuc: " + otp + "\n"
                    + "Ban da yeu cau " + action + " tai DuaStore.\n"
                    + "Ma xac thuc cua ban la: " + otp + "\n"
                    + "Hieu luc trong 5 phut. Khong chia se ma nay voi bat ky ai.";

            helper.setSubject(subject);
            helper.setText(plainText, html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendOrderSuccessEmail(String toEmail, String hoTen, String maDon,
            String ngayDat, String diaChi, String phuongThucTT,
            String phuongThucGH, String tongTien,
            String danhSachSP) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore] Đặt hàng thành công - " + maDon);

            String html = """
                <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
                  <table width="560" align="center"
                         style="background:#fff;border-radius:16px;overflow:hidden;
                                box-shadow:0 4px 24px rgba(0,0,0,.1);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#e53935,#c62828);
                                 padding:32px;text-align:center;">
                        <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                        <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                        <div style="color:#fff;font-size:16px;margin-top:12px;">&#10003; Đặt hàng thành công</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="font-size:15px;color:#616161;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size:14px;color:#616161;line-height:1.6;">
                          Đơn hàng <strong style="color:#e53935;">%s</strong> đã được đặt thành công vào lúc <strong>%s</strong>.
                        </p>

                        <table style="width:100%%;border-collapse:collapse;margin-top:16px;">
                          <tr><td style="padding:6px 0;font-size:13px;color:#9e9e9e;width:120px;">Địa chỉ nhận</td>
                              <td style="padding:6px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:6px 0;font-size:13px;color:#9e9e9e;">Thanh toán</td>
                              <td style="padding:6px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:6px 0;font-size:13px;color:#9e9e9e;">Giao hàng</td>
                              <td style="padding:6px 0;font-size:14px;color:#424242;">%s</td></tr>
                        </table>

                        <div style="border-top:1px solid #eee;margin:20px 0;padding-top:16px;">
                          <div style="font-size:14px;font-weight:600;color:#424242;margin-bottom:12px;">Sản phẩm đã đặt</div>
                          %s
                        </div>

                        <div style="border-top:2px solid #e53935;padding-top:16px;text-align:right;">
                          <span style="font-size:22px;font-weight:800;color:#e53935;">%s</span>
                        </div>

                        <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                          Cảm ơn bạn đã mua sắm tại DuaStore!<br/>
                          Mọi thắc mắc vui lòng liên hệ <strong>%s</strong>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">&copy; 2026 DuaStore</span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(hoTen, maDon, ngayDat, diaChi, phuongThucTT, phuongThucGH, danhSachSP, tongTien, resolveContactPhone());

            helper.setText(toPlainText(html), html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    public void sendPasswordResetSuccess(String toEmail) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore] Đặt lại mật khẩu thành công");
            String contactPhone = resolveContactPhone();
            String plainPasswordReset = "DuaStore\n\nMật khẩu đã được đặt lại thành công.\n"
                    + "Nếu không phải bạn, liên hệ: " + contactPhone;
            helper.setText(plainPasswordReset, """
                <div style="font-family:Arial;padding:20px;">
                  <h2 style="color:#e53935;">DuaStore</h2>
                  <p>Mật khẩu đã được <strong>đặt lại thành công</strong>.</p>
                  <p>Nếu không phải bạn, liên hệ: <strong>%s</strong></p>
                </div>
                """.formatted(contactPhone));
            sender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ─── Email thay đổi trạng thái đơn hàng → gửi cho khách hàng ───────────
    public boolean sendOrderStatusEmail(String toEmail, String hoTen, String maDon, String trangThaiLabel) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore] Cập nhật đơn hàng " + maDon);
            String html = """
                <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
                  <table width="520" align="center"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#e53935,#c62828);padding:28px;text-align:center;">
                        <div style="color:#fff;font-size:22px;font-weight:800;">DuaStore</div>
                        <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px 40px;">
                        <p style="font-size:15px;color:#616161;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size:14px;color:#616161;line-height:1.7;">
                          Đơn hàng <strong style="color:#e53935;">%s</strong> của bạn vừa được cập nhật trạng thái mới:
                        </p>
                        <div style="background:#fff8f8;border-left:4px solid #e53935;border-radius:8px;padding:16px 20px;margin:16px 0;">
                          <div style="font-size:16px;font-weight:700;color:#e53935;">%s</div>
                        </div>
                        <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                          Cảm ơn bạn đã tin tưởng mua sắm tại DuaStore!<br/>
                          Mọi thắc mắc vui lòng liên hệ <strong>%s</strong>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:14px;text-align:center;border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">&copy; 2026 DuaStore</span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(hoTen, maDon, trangThaiLabel, resolveContactPhone());
            helper.setText(toPlainText(html), html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send order status email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    // ─── Email phân công đơn hàng → gửi cho admin/nhân viên được giao ────────
    // Nhan ca Order de hien thi day du chi tiet (khong chi ten khach) — bao gom phan biet
    // ro phuong thuc thanh toan (COD / chuyen khoan thu cong / SePay tu dong), trang thai
    // thanh toan, va don vi van chuyen, thay vi chi 2 dong so sai nhu truoc.
    public boolean sendOrderAssignedEmail(String toEmail, String adminName, Order order, String assignedBy) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore Admin] Đơn hàng " + order.getMaDon() + " cần xử lý");

            String customerName = order.getSnapTenNguoiNhan() != null ? order.getSnapTenNguoiNhan() : "Khách hàng";
            String phone = order.getSnapSoDienThoai() != null ? order.getSnapSoDienThoai() : "";
            String diaChi = order.getSnapDiaChi() != null ? order.getSnapDiaChi() : "";
            String tongTien = com.duastore.util.PriceUtils.format(order.getTongThanhToan()) + "₫";
            String ptTT = paymentMethodLabel(order.getPhuongThucTT());
            boolean daThanhToan = "DA_THANH_TOAN".equals(order.getTrangThaiTT());
            String trangThaiTTLabel = daThanhToan ? "Đã thanh toán" : "Chưa thanh toán";
            String trangThaiTTColor = daThanhToan ? "#2e7d32" : "#e65100";
            String ptGH = shippingLabel(order.getShippingCarrier());
            String ngayDat = order.getNgayDat() != null
                    ? order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
            String orderLink = appUrl + "/admin/don-hang/" + order.getId();

            String html = """
                <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
                  <table width="560" align="center"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#1565c0,#0d47a1);padding:28px;text-align:center;">
                        <div style="color:#fff;font-size:22px;font-weight:800;">DuaStore Admin</div>
                        <div style="color:rgba(255,255,255,.8);font-size:13px;">Hệ thống quản lý đơn hàng</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px 40px;">
                        <p style="font-size:15px;color:#616161;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size:14px;color:#616161;line-height:1.7;">
                          Bạn vừa được <strong>%s</strong> phân công xử lý đơn hàng sau:
                        </p>
                        <table style="width:100%%;border-collapse:collapse;background:#f0f6ff;border-radius:8px;margin:16px 0;">
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;width:140px;border-left:4px solid #1565c0;">Mã đơn hàng</td>
                              <td style="padding:8px 20px 8px 0;font-size:15px;font-weight:700;color:#1565c0;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Ngày đặt</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Khách hàng</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">SĐT</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Địa chỉ giao</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Phương thức TT</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Trạng thái TT</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;font-weight:700;color:%s;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Vận chuyển</td>
                              <td style="padding:8px 20px 8px 0;font-size:14px;color:#424242;">%s</td></tr>
                          <tr><td style="padding:8px 20px;font-size:13px;color:#9e9e9e;border-left:4px solid #1565c0;">Tổng tiền</td>
                              <td style="padding:8px 20px 8px 0;font-size:16px;font-weight:800;color:#e53935;">%s</td></tr>
                        </table>
                        <p style="text-align:center;margin:24px 0 8px;">
                          <a href="%s" style="display:inline-block;padding:10px 24px;background:#1565c0;color:#fff;text-decoration:none;border-radius:6px;font-weight:600;">Xem &amp; xử lý đơn hàng</a>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:14px;text-align:center;border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">&copy; 2026 DuaStore</span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(adminName, assignedBy, order.getMaDon(), ngayDat, customerName, phone, diaChi,
                        ptTT, trangThaiTTColor, trangThaiTTLabel, ptGH, tongTien, orderLink);
            helper.setText(toPlainText(html), html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send order assigned email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    // ─── Email cảm ơn + mời đánh giá → gửi cho khách khi đơn chuyển "Đã hoàn thành" ────
    public boolean sendOrderCompletedEmail(String toEmail, String hoTen, String maDon, List<OrderItem> items) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore] Cảm ơn bạn đã mua sắm - " + maDon);

            StringBuilder itemsHtml = new StringBuilder();
            if (items != null) {
                for (OrderItem item : items) {
                    String reviewLink = appUrl + "/san-pham/" + item.getProductId() + "#reviews-section";
                    itemsHtml.append("""
                        <div style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #f0f0f0;">
                          <div>
                            <div style="font-size:14px;color:#424242;">%s</div>
                            <div style="font-size:12px;color:#9e9e9e;">%s x %s</div>
                          </div>
                          <a href="%s" style="font-size:13px;color:#fff;background:#f9a825;padding:6px 14px;border-radius:20px;text-decoration:none;font-weight:600;white-space:nowrap;">&#9733; Đánh giá</a>
                        </div>
                        """.formatted(item.getTenSanPham(), item.getTenBienThe() != null ? item.getTenBienThe() : "",
                            item.getSoLuong(), reviewLink));
                }
            }

            String html = """
                <html><body style="font-family:Arial;background:#f5f5f5;padding:40px 0;">
                  <table width="560" align="center"
                         style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1);">
                    <tr>
                      <td style="background:linear-gradient(135deg,#f9a825,#f57f17);padding:32px;text-align:center;">
                        <div style="color:#fff;font-size:24px;font-weight:800;">DuaStore</div>
                        <div style="color:rgba(255,255,255,.85);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                        <div style="color:#fff;font-size:16px;margin-top:12px;">&#127881; Đơn hàng đã hoàn thành!</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="font-size:15px;color:#616161;">Xin chào <strong>%s</strong>,</p>
                        <p style="font-size:14px;color:#616161;line-height:1.6;">
                          Cảm ơn bạn đã tin tưởng và mua sắm tại DuaStore! Đơn hàng <strong style="color:#e53935;">%s</strong> đã hoàn tất.
                          Bạn nghĩ sao về sản phẩm đã nhận? Hãy dành chút thời gian đánh giá để giúp DuaStore phục vụ tốt hơn:
                        </p>
                        <div style="margin-top:16px;">
                          %s
                        </div>
                        <p style="font-size:13px;color:#9e9e9e;margin-top:24px;">
                          Cảm ơn bạn đã mua sắm tại DuaStore!<br/>
                          Mọi thắc mắc vui lòng liên hệ <strong>%s</strong>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">&copy; 2026 DuaStore</span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(hoTen, maDon, itemsHtml.toString(), resolveContactPhone());
            helper.setText(toPlainText(html), html);
            sender.send(msg);
            return true;
        } catch (Exception e) {
            log.warn("Failed to send order completed email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private static String toPlainText(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String text = html.replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?s)<[^>]+>", "\n")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("\u00a0", " ");
        text = text.replaceAll("(?m)\\s*\\n\\s*\\n+", "\n\n")
                .replaceAll("(?m)^[ \\t]+", "")
                .replaceAll("(?m)[ \\t]+$", "")
                .trim();
        return text;
    }
}
