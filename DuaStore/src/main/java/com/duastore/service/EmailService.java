package com.duastore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@Service
public class EmailService {

    private final JavaMailSender defaultMailSender;
    private final SiteSettingService siteSettingService;

    @Value("${spring.mail.username}")
    private String defaultFromEmail;

    private JavaMailSenderImpl dynamicSender;
    private String lastSettingsHash = "";

    public EmailService(JavaMailSender mailSender, SiteSettingService siteSettingService) {
        this.defaultMailSender = mailSender;
        this.siteSettingService = siteSettingService;
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

    public void send(String to, String subject, String body) {
        try {
            JavaMailSender sender = resolveMailSender();
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(resolveFromEmail(), resolveFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            sender.send(msg);
        } catch (Exception ignored) {
        }
    }

    public void sendOtpEmail(String toEmail, String otp, String purpose) {
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
                        <div style="color:rgba(255,255,255,.8);font-size:13px;">Đồ Thủy Tinh Cao Cấp</div>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="font-size:15px;color:#616161;">
                          Bạn đã yêu cầu <strong>%s</strong> tại DuaStore.<br/>
                          Vui lòng dùng mã OTP bên dưới:
                        </p>
                        <div style="background:#fff8f8;border:2px dashed #e53935;
                                    border-radius:12px;padding:24px;text-align:center;">
                          <div style="font-size:13px;color:#9e9e9e;letter-spacing:2px;">
                            MÃ XÁC THỰC
                          </div>
                          <div style="font-size:44px;font-weight:900;letter-spacing:12px;
                                      color:#e53935;font-family:monospace;">
                            %s
                          </div>
                          <div style="font-size:12px;color:#9e9e9e;margin-top:8px;">
                            Hiệu lực trong <strong>5 phút</strong>
                          </div>
                        </div>
                        <p style="font-size:12px;color:#9e9e9e;margin-top:20px;">
                          Không chia sẻ mã này với bất kỳ ai.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:16px;text-align:center;
                                 border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">
                          &copy; 2025 DuaStore
                        </span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(action, otp);

            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(msg);

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    public void sendOrderSuccessEmail(String toEmail, String hoTen, String maDon,
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
                          Mọi thắc mắc vui lòng liên hệ <strong>0901 234 567</strong>
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#f9f9f9;padding:16px;text-align:center;border-top:1px solid #eee;">
                        <span style="font-size:12px;color:#bdbdbd;">&copy; 2025 DuaStore</span>
                      </td>
                    </tr>
                  </table>
                </body></html>
                """.formatted(hoTen, maDon, ngayDat, diaChi, phuongThucTT, phuongThucGH, danhSachSP, tongTien);

            helper.setText(html, true);
            sender.send(msg);
        } catch (Exception ignored) {
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
            helper.setText("""
                <div style="font-family:Arial;padding:20px;">
                  <h2 style="color:#e53935;">DuaStore</h2>
                  <p>Mật khẩu đã được <strong>đặt lại thành công</strong>.</p>
                  <p>Nếu không phải bạn, liên hệ: <strong>0901 234 567</strong></p>
                </div>
                """, true);
            sender.send(msg);
        } catch (Exception ignored) {
        }
    }
}
