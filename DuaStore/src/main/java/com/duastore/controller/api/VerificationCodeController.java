package com.duastore.controller.api;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.EmailService;
import com.duastore.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class VerificationCodeController {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeController.class);
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

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.ok(Map.of("success", false, "error", "Email đã được sử dụng"));
        }

        String code = codeService.generate(email);
        log.info("OTP sent to {}", email);

        try {
            emailService.sendOtpEmail(email, code, "REGISTER");
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("Failed to send email to {}", email, e);
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
