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
        try {
            emailService.sendOtpEmail(email, code, "REGISTER");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Không thể gửi email. Vui lòng thử lại sau."));
        }
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
