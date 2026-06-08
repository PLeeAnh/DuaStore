package com.duastore.controller.client;

import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/quen-mat-khau")
    public String forgotForm(Model model) {
        model.addAttribute("title", "Quên mật khẩu");
        return "view/auth/forgot-password";
    }

    @PostMapping("/quen-mat-khau")
    public String forgotSubmit(@RequestParam String email, RedirectAttributes ra) {
        Optional<User> opt = userRepository.findByEmail(email);
        if (opt.isPresent()) {
            User user = opt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.send(email, "Đặt lại mật khẩu DuaStore",
                    "<h2>Đặt lại mật khẩu</h2>"
                    + "<p>Nhấp vào link dưới đây để đặt lại mật khẩu của bạn:</p>"
                    + "<a href='http://localhost:8080/dat-lai-mat-khau?token=" + token + "'>"
                    + "Đặt lại mật khẩu</a>"
                    + "<p>Link có hiệu lực trong 1 giờ.</p>"
                    + "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>");
        }
        ra.addFlashAttribute("successMsg", "Nếu email tồn tại, bạn sẽ nhận được hướng dẫn đặt lại mật khẩu");
        return "redirect:/quen-mat-khau";
    }

    @GetMapping("/dat-lai-mat-khau")
    public String resetForm(@RequestParam String token, Model model) {
        Optional<User> opt = userRepository.findByResetToken(token);
        if (opt.isEmpty() || opt.get().getResetTokenExpiry() == null
                || opt.get().getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            model.addAttribute("errorMsg", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
            model.addAttribute("title", "Đặt lại mật khẩu");
            return "view/auth/reset-password";
        }
        model.addAttribute("title", "Đặt lại mật khẩu");
        model.addAttribute("token", token);
        return "view/auth/reset-password";
    }

    @PostMapping("/dat-lai-mat-khau")
    public String resetSubmit(@RequestParam String token, @RequestParam String password,
                              @RequestParam String confirmPassword, RedirectAttributes ra) {
        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu không khớp");
            return "redirect:/dat-lai-mat-khau?token=" + token;
        }

        Optional<User> opt = userRepository.findByResetToken(token);
        if (opt.isEmpty() || opt.get().getResetTokenExpiry() == null
                || opt.get().getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("errorMsg", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn");
            return "redirect:/dang-nhap";
        }

        User user = opt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        ra.addFlashAttribute("successMsg", "Mật khẩu đã được đặt lại thành công");
        return "redirect:/dang-nhap";
    }
}
