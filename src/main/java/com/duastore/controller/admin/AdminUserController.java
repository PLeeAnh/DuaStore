package com.duastore.controller.admin;

import com.duastore.model.Role;
import com.duastore.model.User;
import com.duastore.config.security.SecurityUtil;
import com.duastore.repository.UserRepository;
import com.duastore.service.NotificationHelper;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/nguoi-dung")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới người dùng.
 */
public class AdminUserController {

    private final UserRepository userRepository;
    private final AdminUserService adminUserService;
    private final AdminLogService adminLogService;
    private final SecurityUtil securityUtil;
    private final NotificationHelper notificationHelper;

    public AdminUserController(UserRepository userRepository, AdminUserService adminUserService, AdminLogService adminLogService, SecurityUtil securityUtil,
            NotificationHelper notificationHelper) {
        this.userRepository = userRepository;
        this.adminUserService = adminUserService;
        this.adminLogService = adminLogService;
        this.securityUtil = securityUtil;
        this.notificationHelper = notificationHelper;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_READ)")
    public String list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String role, Model model) {
        Sort sort = Sort.by(Sort.Direction.DESC, "ngayTao");
        Page<User> userPage;
        if (role != null && !role.isEmpty()) {
            userPage = userRepository.findByRole(role, PageRequest.of(page, size, sort));
        } else {
            userPage = userRepository.findAllBy(PageRequest.of(page, size, sort));
        }
        model.addAttribute("title", "nguoi-dung");
        model.addAttribute("userTab", "nguoi-dung");
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "người dùng");
        model.addAttribute("url", "/admin/nguoi-dung");
        Map<String, Object> filterParams = new java.util.HashMap<>();
        if (role != null && !role.isEmpty()) {
            filterParams.put("role", role);
        }
        model.addAttribute("filterParams", filterParams);
        model.addAttribute("role", role);
        model.addAttribute("allRoles", adminUserService.getAllRoles());
        model.addAttribute("productOwnerCount", adminUserService.countActiveByRole("PRODUCT_OWNER"));
        model.addAttribute("adminCount", adminUserService.countActiveByRole("ADMIN"));
        model.addAttribute("staffCount", adminUserService.countActiveByRole("STAFF"));
        model.addAttribute("userCount", adminUserService.countActiveByRole("USER"));
        return "view/admin/user/user-list";
    }

    @PostMapping("/toggle-status")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_UPDATE)")
    public String toggleStatus(@RequestParam Integer id, RedirectAttributes ra) {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng hiện tại");
                return "redirect:/admin/nguoi-dung";
            }
            User target = adminUserService.getUserById(id);
            boolean oldActive = target.getIsActive();
            adminUserService.toggleStatus(id, admin);
            boolean newActive = !oldActive;
            notificationHelper.notifyAll(
                    newActive ? "Tai khoan cua ban da duoc kich hoat" : "Tai khoan cua ban da bi khoa",
                    null, null, null, null,
                    target.getId()
            );
            notificationHelper.notifyStaff(
                    "Admin " + admin.getHoTen() + " da " + (newActive ? "kich hoat" : "khoa")
                            + " tai khoan khach hang " + target.getHoTen(),
                    null, null,
                    "/admin/khach-hang/" + target.getId(),
                    "Xem khach hang",
                    com.duastore.config.security.PermissionEnum.CUSTOMER_READ
            );
            adminLogService.ghiLog(admin, oldActive ? "KHOA_USER" : "KICH_HOAT_USER", "USER", id, String.valueOf(oldActive), String.valueOf(!oldActive), (oldActive ? "Khóa" : "Kích hoạt") + " tài khoản " + target.getHoTen());
            ra.addFlashAttribute("successMsg", "Cập nhật trạng thái thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            User user = adminUserService.getUserById(id);
            model.addAttribute("title", "nguoi-dung");
            model.addAttribute("user", user);
            model.addAttribute("allRoles", adminUserService.getAllRoles());
            return "view/admin/user/user-form";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/nguoi-dung";
        }
    }

    @GetMapping("/tao-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "nguoi-dung");
        model.addAttribute("allRoles", adminUserService.getAllRoles());
        return "view/admin/user/user-create";
    }

    @PostMapping("/tao-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_CREATE)")
    public String create(@RequestParam String username,
                         @RequestParam String hoTen,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String soDienThoai,
                         @RequestParam(required = false) Boolean isActive,
                         @RequestParam(required = false) List<Integer> roleIds,
                         RedirectAttributes ra) {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng hiện tại");
                return "redirect:/admin/nguoi-dung";
            }
            User newUser = adminUserService.createUser(username, hoTen, email, password, soDienThoai, isActive, roleIds, admin);
            adminLogService.ghiLog(admin, "TAO_USER", "USER", newUser.getId(), null, username, "Tạo tài khoản mới " + hoTen);
            ra.addFlashAttribute("successMsg", "Tạo tài khoản thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/nguoi-dung/tao-moi";
        }
        return "redirect:/admin/nguoi-dung";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).USER_UPDATE)")
    public String edit(@PathVariable Integer id, @RequestParam String hoTen, @RequestParam String email, @RequestParam(required = false) String soDienThoai, @RequestParam(required = false) Boolean isActive, @RequestParam(required = false) List<Integer> roleIds, RedirectAttributes ra) {
        try {
            User admin = securityUtil.getCurrentUser();
            if (admin == null) {
                ra.addFlashAttribute("errorMsg", "Không xác định được người dùng hiện tại");
                return "redirect:/admin/nguoi-dung";
            }
            User target = adminUserService.getUserById(id);
            Set<String> oldRoleNames = target.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            adminUserService.updateUser(id, hoTen, email, soDienThoai, isActive, admin);
            adminUserService.updateUserRoles(id, roleIds, admin);
            Set<String> newRoleNames = roleIds != null ? adminUserService.getUserById(id).getRoles().stream().map(Role::getName).collect(Collectors.toSet()) : Set.of();
            String moTa = "Cập nhật thông tin người dùng " + target.getHoTen();
            if (!oldRoleNames.equals(newRoleNames)) {
                String rolesCu = oldRoleNames.isEmpty() ? "không có vai trò" : String.join(", ", oldRoleNames);
                String rolesMoi = newRoleNames.isEmpty() ? "không có vai trò" : String.join(", ", newRoleNames);
                moTa = "Cập nhật vai trò của " + target.getHoTen() + ": " + rolesCu + " → " + rolesMoi;
            }
            adminLogService.ghiLog(admin, "SUA_USER", "USER", id, oldRoleNames.isEmpty() ? null : String.join(",", oldRoleNames), newRoleNames.isEmpty() ? null : String.join(",", newRoleNames), moTa);
            ra.addFlashAttribute("successMsg", "Cập nhật người dùng thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/nguoi-dung/sua/" + id;
        }
        return "redirect:/admin/nguoi-dung";
    }
}
