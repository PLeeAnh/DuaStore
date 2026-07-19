package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.TwoFactorAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/2fa")
public class AdminTwoFactorController {

    private final TwoFactorAuthService twoFactorAuthService;
    private final SecurityUtil securityUtil;

    public AdminTwoFactorController(TwoFactorAuthService twoFactorAuthService,
                                    SecurityUtil securityUtil) {
        this.twoFactorAuthService = twoFactorAuthService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/setup")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public String setupForm(Model model) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        boolean enabled = twoFactorAuthService.isTwoFactorEnabled(userId);
        model.addAttribute("twoFactorEnabled", enabled);

        if (!enabled) {
            String secret = twoFactorAuthService.generateSecret();
            String email = securityUtil.getCurrentUser().getEmail();
            String qrCode = twoFactorAuthService.getQRCode(secret, email);
            model.addAttribute("secret", secret);
            model.addAttribute("qrCode", qrCode);
        }

        model.addAttribute("title", "2fa");
        return "view/admin/security/2fa-setup";
    }

    @PostMapping("/enable")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public String enable(@RequestParam String secret,
                         @RequestParam int code,
                         RedirectAttributes ra,
                         HttpSession session) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        if (!twoFactorAuthService.verify(secret, code)) {
            ra.addFlashAttribute("errorMsg", "Mã xác thực không đúng. Vui lòng thử lại.");
            return "redirect:/admin/2fa/setup";
        }

        twoFactorAuthService.enableTwoFactor(userId, secret);
        ra.addFlashAttribute("successMsg", "Đã bật xác thực hai yếu tố thành công.");
        return "redirect:/admin/2fa/setup";
    }

    @PostMapping("/disable")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public String disable(@RequestParam int code,
                          RedirectAttributes ra) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        String secret = securityUtil.getCurrentUser().getTwoFactorSecret();
        if (secret == null || !twoFactorAuthService.verify(secret, code)) {
            ra.addFlashAttribute("errorMsg", "Mã xác thực không đúng.");
            return "redirect:/admin/2fa/setup";
        }

        twoFactorAuthService.disableTwoFactor(userId);
        ra.addFlashAttribute("successMsg", "Đã tắt xác thực hai yếu tố.");
        return "redirect:/admin/2fa/setup";
    }

    @GetMapping("/challenge")
    public String challengeForm(Model model) {
        model.addAttribute("title", "2fa-challenge");
        return "view/admin/security/2fa-challenge";
    }

    @PostMapping("/challenge")
    public String verifyChallenge(@RequestParam int code,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("2faUserId");

        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        String secret = twoFactorAuthService.getSecret(userId);
        if (secret == null) {
            return "redirect:/dang-nhap";
        }

        if (!twoFactorAuthService.verify(secret, code)) {
            ra.addFlashAttribute("errorMsg", "Mã xác thực không đúng.");
            return "redirect:/admin/2fa/challenge";
        }

        session.setAttribute("2faVerified", true);
        session.setAttribute("2faVerifiedAt", java.time.Instant.now().toString());
        session.removeAttribute("2faUserId");
        return "redirect:/admin";
    }
}
