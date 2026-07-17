package com.duastore.controller.client;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.repository.RoleRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.VerificationCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final VerificationCodeService verifyCodeService;
    private final NotificationHelper notificationHelper;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            VerificationCodeService verifyCodeService,
            NotificationHelper notificationHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.verifyCodeService = verifyCodeService;
        this.notificationHelper = notificationHelper;
    }

    @GetMapping("/oauth2/success")
    public String oauth2Success() {
        return "view/auth/oauth2-success";
    }

    @GetMapping("/dang-nhap")
    public String login(Model model) {
        model.addAttribute("title", "Đăng nhập");
        return "view/auth/login";
    }

    @GetMapping("/dang-ky")
    public String registerForm(Model model) {
        model.addAttribute("title", "Đăng ký");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "view/auth/register";
    }

    @PostMapping("/dang-ky")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest req,
            BindingResult result,
            @RequestParam("verificationCode") String verificationCode,
            RedirectAttributes ra,
            Model model) {
        model.addAttribute("title", "Đăng ký");

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error", "Mật khẩu xác nhận không khớp");
        }

        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            result.rejectValue("username", "error", "Tên đăng nhập đã tồn tại");
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            result.rejectValue("email", "error", "Email đã được sử dụng");
        }

        if (!verifyCodeService.verify(req.getEmail(), verificationCode)) {
            result.rejectValue("email", "error", "Mã xác thực không đúng hoặc đã hết hạn");
        }

        if (result.hasErrors()) {
            return "view/auth/register";
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setHoTen(req.getHoTen());
        user.setSoDienThoai(req.getSoDienThoai());
        Role userRole = roleRepository.findByName("USER");
        user.setRoles(Set.of(userRole));
        user.setIsActive(true);
        User savedUser = userRepository.save(user);
        notificationHelper.notifyStaff(
                "Khach hang moi: " + savedUser.getHoTen() + " (" + savedUser.getEmail() + ")",
                null, null,
                "/admin/khach-hang/" + savedUser.getId(),
                "Xem khach hang"
        );
        verifyCodeService.delete(req.getEmail());

        ra.addFlashAttribute("successMsg", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/dang-nhap";
    }

    public static class RegisterRequest {

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3-50 ký tự")
        private String username;

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        private String email;

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$",
                message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số")
        private String password;

        private String confirmPassword;

        @NotBlank(message = "Họ tên không được để trống")
        private String hoTen;

        private String soDienThoai;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getHoTen() {
            return hoTen;
        }

        public void setHoTen(String hoTen) {
            this.hoTen = hoTen;
        }

        public String getSoDienThoai() {
            return soDienThoai;
        }

        public void setSoDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
        }
    }
}
