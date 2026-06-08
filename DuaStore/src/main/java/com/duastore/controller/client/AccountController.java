package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccountController {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(SecurityUtil securityUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/tai-khoan")
    public String profile(Model model) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("title", "Hồ sơ");
        model.addAttribute("user", user);
        return "view/client/account/profile";
    }

    @PostMapping("/tai-khoan/cap-nhat")
    public String updateProfile(@RequestParam String hoTen, @RequestParam(required = false) String soDienThoai,
                                RedirectAttributes ra, HttpServletRequest request) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        if (!StringUtils.hasText(hoTen) || hoTen.trim().length() > 100) {
            ra.addFlashAttribute("errorMsg", "Họ tên không hợp lệ (1-100 ký tự)");
            return "redirect:/tai-khoan";
        }
        if (soDienThoai != null && !soDienThoai.isBlank() && !soDienThoai.matches("(84|0)[0-9]{9}")) {
            ra.addFlashAttribute("errorMsg", "Số điện thoại không hợp lệ");
            return "redirect:/tai-khoan";
        }
        user.setHoTen(hoTen.trim());
        user.setSoDienThoai(soDienThoai != null ? soDienThoai.trim() : null);
        userRepository.save(user);
        HttpSession session = request.getSession();
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userPhone", user.getSoDienThoai());
        session.setAttribute("userInitial", user.getHoTen() != null && !user.getHoTen().isEmpty()
                ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase() : "U");
        ra.addFlashAttribute("successMsg", "Cập nhật hồ sơ thành công");
        return "redirect:/tai-khoan";
    }

    @GetMapping("/tai-khoan/doi-mat-khau")
    public String changePasswordForm(Model model) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("title", "Đổi mật khẩu");
        return "view/client/account/change-password";
    }

    @PostMapping("/tai-khoan/doi-mat-khau")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword,
                                 @RequestParam String confirmPassword, RedirectAttributes ra) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu cũ không đúng");
            return "redirect:/tai-khoan/doi-mat-khau";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu mới không khớp");
            return "redirect:/tai-khoan/doi-mat-khau";
        }
        if (newPassword.length() < 6) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu tối thiểu 6 ký tự");
            return "redirect:/tai-khoan/doi-mat-khau";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        ra.addFlashAttribute("successMsg", "Đổi mật khẩu thành công");
        return "redirect:/tai-khoan";
    }
}
