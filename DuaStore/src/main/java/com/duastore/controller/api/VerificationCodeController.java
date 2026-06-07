package com.duastore.controller.api;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.EmailService;
import com.duastore.service.VerificationCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class VerificationCodeController {

    private final VerificationCodeService codeService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public VerificationCodeController(VerificationCodeService codeService,
                                      EmailService emailService,
                                      UserRepository userRepository) {
        this.codeService = codeService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email không được bỏ trống"));
        }

        String code = codeService.generate(email);
        emailService.send(email, "Mã xác thực DuaStore",
                "<h2>Xác thực tài khoản DuaStore</h2>"
                + "<p>Mã xác thực của bạn là:</p>"
                + "<h1 style='color:#d4a017; letter-spacing:8px;'>" + code + "</h1>"
                + "<p>Mã có hiệu lực trong 5 phút.</p>"
                + "<p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.</p>");

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin"));
        }

        boolean valid = codeService.verify(email, code);
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mã xác thực không đúng hoặc đã hết hạn"));
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
