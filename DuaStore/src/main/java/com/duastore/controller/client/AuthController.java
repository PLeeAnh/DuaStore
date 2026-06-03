package com.duastore.controller.client;

import com.duastore.model.User;
import com.duastore.service.client.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/dang-nhap")
    public String showLoginForm() {
        return "view/client/auth/login";
    }

    @PostMapping("/dang-nhap")
    public String processLogin(@RequestParam("usernameOrEmail") String usernameOrEmail,
                               @RequestParam("password") String password,
                               HttpServletRequest request,
                               Model model) {
        try {
            User user = authService.authenticate(usernameOrEmail, password);

            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("loggedIn", true);
            session.setAttribute("role", user.getRole());
            session.setAttribute("userEmail", user.getEmail());
            String initial = user.getHoTen() != null && !user.getHoTen().isEmpty()
                    ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase()
                    : "U";
            session.setAttribute("userInitial", initial);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin";
            }
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "view/client/auth/login";
        }
    }

    @GetMapping("/dang-ky")
    public String showRegisterForm() {
        return "view/client/auth/register";
    }

    @PostMapping("/dang-ky")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("hoTen") String hoTen,
                                  @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
                                  HttpServletRequest request,
                                  RedirectAttributes ra) {
        try {
            User user = authService.register(username, email, password, hoTen, soDienThoai);
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("loggedIn", true);
            session.setAttribute("role", user.getRole());
            session.setAttribute("userEmail", user.getEmail());
            String initial = user.getHoTen() != null && !user.getHoTen().isEmpty()
                    ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase()
                    : "U";
            session.setAttribute("userInitial", initial);
            return "redirect:/";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/dang-ky";
        }
    }

    @GetMapping("/dang-xuat")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
