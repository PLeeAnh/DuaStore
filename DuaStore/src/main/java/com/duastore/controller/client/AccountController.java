package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.User;
import com.duastore.model.LinkedAccount;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.LinkedAccountRepository;
import com.duastore.repository.OrderRepository;
import com.duastore.repository.ReviewsRepository;
import com.duastore.repository.UserAuthProviderRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.FileUploadService;
import com.duastore.service.LinkedAccountService;
import com.duastore.service.LocationService;
import com.duastore.service.UserSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AccountController {

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final LocationService locationService;
    private final LinkedAccountService linkedAccountService;
    private final UserSettingService userSettingService;
    private final FileUploadService fileUploadService;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final LinkedAccountRepository linkedAccountRepository;
    private final OrderRepository orderRepository;
    private final ReviewsRepository reviewsRepository;

    public AccountController(SecurityUtil securityUtil, UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AddressRepository addressRepository, LocationService locationService,
            LinkedAccountService linkedAccountService,
            UserSettingService userSettingService,
            FileUploadService fileUploadService,
            UserAuthProviderRepository userAuthProviderRepository,
            LinkedAccountRepository linkedAccountRepository,
            OrderRepository orderRepository,
            ReviewsRepository reviewsRepository) {
        this.securityUtil = securityUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
        this.locationService = locationService;
        this.linkedAccountService = linkedAccountService;
        this.userSettingService = userSettingService;
        this.fileUploadService = fileUploadService;
        this.userAuthProviderRepository = userAuthProviderRepository;
        this.linkedAccountRepository = linkedAccountRepository;
        this.orderRepository = orderRepository;
        this.reviewsRepository = reviewsRepository;
    }

    @GetMapping("/tai-khoan")
    public String profile(Model model) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("title", "Tài khoản");
        model.addAttribute("user", user);
        model.addAttribute("authProviders", userAuthProviderRepository.findByUserId(user.getId()));
        return "view/client/account/profile";
    }

    @PostMapping("/tai-khoan/cap-nhat")
    public String updateProfile(@RequestParam String hoTen,
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false, defaultValue = "false") boolean emailVisible,
            @RequestParam(required = false, defaultValue = "false") boolean phoneVisible,
            @RequestParam(required = false, defaultValue = "true") boolean emailMarketing,
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
        user.setNickname(StringUtils.hasText(nickname) ? nickname.trim() : null);
        user.setEmailVisible(emailVisible);
        user.setPhoneVisible(phoneVisible);
        user.setEmailMarketing(emailMarketing);
        userRepository.save(user);
        HttpSession session = request.getSession();
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userPhone", user.getSoDienThoai());
        session.setAttribute("userNickname", user.getNickname());
        session.setAttribute("userInitial", user.getHoTen() != null && !user.getHoTen().isEmpty()
                ? String.valueOf(user.getHoTen().charAt(0)).toUpperCase() : "U");
        ra.addFlashAttribute("successMsg", "Cập nhật thành công");
        return "redirect:/tai-khoan";
    }

    @GetMapping("/tai-khoan/doi-mat-khau")
    public String changePasswordForm(Model model) {
        if (securityUtil.getCurrentUser() == null) {
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
            return "redirect:/tai-khoan?tab=security";
        }
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMsg", "Mật khẩu mới không khớp");
            return "redirect:/tai-khoan?tab=security";
        }
        if (newPassword.length() < 8) {
                ra.addFlashAttribute("errorMsg", "Mật khẩu tối thiểu 8 ký tự");
            return "redirect:/tai-khoan?tab=security";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        ra.addFlashAttribute("successMsg", "Đổi mật khẩu thành công");
        return "redirect:/tai-khoan";
    }

    @PostMapping("/tai-khoan/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") boolean remove,
            HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        try {
            if (remove) {
                if (user.getAvatar() != null) {
                    fileUploadService.delete(user.getAvatar(), "avatars");
                }
                user.setAvatar(null);
            } else if (file != null && !file.isEmpty()) {
                if (user.getAvatar() != null) {
                    fileUploadService.delete(user.getAvatar(), "avatars");
                }
                String url = fileUploadService.save(file, "avatars");
                user.setAvatar(url);
            } else {
                res.put("success", false);
                res.put("message", "Vui lòng chọn ảnh");
                return ResponseEntity.ok(res);
            }
            userRepository.save(user);
            HttpSession session = request.getSession();
            session.setAttribute("userAvatar", user.getAvatar());
            res.put("success", true);
            res.put("avatar", user.getAvatar());
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/trang-thai")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestParam String status,
            HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        user.setStatus(status);
        userRepository.save(user);
        request.getSession().setAttribute("userStatus", status);
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/tai-khoan/cai-dat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSettings() {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(Map.of());
        }
        return ResponseEntity.ok(userSettingService.getSettings(user.getId()));
    }

    @PostMapping("/tai-khoan/cai-dat")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveSetting(@RequestParam String key,
            @RequestParam(required = false) String value) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        userSettingService.setSetting(user.getId(), key, value != null ? value : "");
        res.put("success", true);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/tai-khoan-lien-ket")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> linkAccount(@RequestParam String username,
            @RequestParam String password) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.ok(res);
        }
        try {
            linkedAccountService.linkAccount(user.getId(), username, password);
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/tai-khoan-lien-ket/xoa/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unlinkAccount(@PathVariable Integer id) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        try {
            linkedAccountService.unlinkAccount(user.getId(), id);
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/chuyen-doi/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> switchAccount(@PathVariable Integer id,
            HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        try {
            linkedAccountService.switchAccount(user.getId(), id, request);
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.setAttribute("hasGoogleLinked", userAuthProviderRepository.existsByUserIdAndProvider(id, "GOOGLE"));
            }
            res.put("success", true);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/dang-xuat-tat-ca")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logoutAll(HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        res.put("success", true);
        res.put("redirect", "/dang-nhap");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/tai-khoan/api/tai-khoan-lien-ket")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getLinkedAccounts() {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(List.of());
        }
        List<LinkedAccount> links = linkedAccountRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = links.stream().map(link -> {
            User linked = userRepository.findById(link.getLinkedUserId()).orElse(null);
            if (linked == null) {
                return null;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", linked.getId());
            m.put("hoTen", linked.getHoTen());
            m.put("email", linked.getEmail());
            return m;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tai-khoan/api/hoat-dong")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getActivity() {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return ResponseEntity.ok(res);
        }
        List<Map<String, Object>> orders = orderRepository.findByUserId(user.getId(), PageRequest.of(0, 5)).getContent()
                .stream().map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("maDon", o.getMaDon());
                    m.put("tongThanhToan", o.getTongThanhToan());
                    m.put("ngayDat", o.getNgayDat());
                    m.put("trangThaiDon", o.getTrangThaiDon());
                    return m;
                }).collect(Collectors.toList());
        List<Map<String, Object>> reviews = reviewsRepository.findByUserIdOrderByNgayTaoDesc(user.getId())
                .stream().limit(5).map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("productId", r.getProductId());
            m.put("danhGia", r.getDanhGia());
            m.put("noiDung", r.getBinhLuan());
            m.put("ngayTao", r.getNgayTao());
            return m;
        }).collect(Collectors.toList());
        res.put("orders", orders);
        res.put("reviews", reviews);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/tai-khoan/vo-hieu-hoa")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateAccount(HttpServletRequest request) {
        Map<String, Object> res = new HashMap<>();
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            res.put("success", false);
            return ResponseEntity.ok(res);
        }
        user.setIsActive(false);
        userRepository.save(user);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        res.put("success", true);
        res.put("redirect", "/dang-nhap");
        return ResponseEntity.ok(res);
    }

    @GetMapping("/tai-khoan/dia-chi")
    public String addresses(Model model) {
        User user = securityUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("title", "Địa chỉ");
        model.addAttribute("user", user);
        model.addAttribute("addresses", addressRepository.findByUserIdOrderByIsDefaultDesc(user.getId()));
        model.addAttribute("provinces", locationService.getProvinces());
        return "view/client/account/addresses";
    }
}
