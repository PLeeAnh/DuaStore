package com.duastore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, "DuaStore");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(msg);
        } catch (Exception ignored) {}
    }

    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, "DuaStore");
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
            mailSender.send(msg);

        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }

    public void sendPasswordResetSuccess(String toEmail) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail, "DuaStore");
            helper.setTo(toEmail);
            helper.setSubject("[DuaStore] Đặt lại mật khẩu thành công");
            helper.setText("""
                <div style="font-family:Arial;padding:20px;">
                  <h2 style="color:#e53935;">DuaStore</h2>
                  <p>Mật khẩu đã được <strong>đặt lại thành công</strong>.</p>
                  <p>Nếu không phải bạn, liên hệ: <strong>0901 234 567</strong></p>
                </div>
                """, true);
            mailSender.send(msg);
        } catch (Exception ignored) {}
    }
}
