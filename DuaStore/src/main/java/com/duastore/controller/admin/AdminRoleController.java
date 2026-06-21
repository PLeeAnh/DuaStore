package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.service.admin.AdminLogService;
import com.duastore.service.admin.AdminRoleService;
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

@Controller
@RequestMapping("/admin/vai-tro")
public class AdminRoleController {

    private final AdminRoleService roleService;
    private final AdminLogService adminLogService;
    private final SecurityUtil securityUtil;

    public AdminRoleController(AdminRoleService roleService,
                               AdminLogService adminLogService,
                               SecurityUtil securityUtil) {
        this.roleService = roleService;
        this.adminLogService = adminLogService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_READ)")
    public String list(Model model) {
        model.addAttribute("title", "vai-tro");
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("entityLabel", "vai trò");
        model.addAttribute("url", "/admin/vai-tro");
        return "view/admin/role/role-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "vai-tro");
        model.addAttribute("role", new com.duastore.model.Role());
        model.addAttribute("groupedPermissions", roleService.getPermissionsGroupedByModule());
        return "view/admin/role/role-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_CREATE)")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String moTa,
                         @RequestParam(required = false) List<Integer> permissionIds,
                         RedirectAttributes ra) {
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Tên vai trò không được để trống");
            return "redirect:/admin/vai-tro/them-moi";
        }
        var saved = roleService.save(null, name.trim().toUpperCase(), moTa, permissionIds);
        var admin = securityUtil.getCurrentUser();
        if (admin != null) {
            adminLogService.ghiLog(admin, "TAO_ROLE", "ROLE", saved.getId(),
                    null, saved.getName(), "Tạo vai trò " + saved.getName());
        }
        ra.addFlashAttribute("successMsg", "Thêm vai trò thành công");
        return "redirect:/admin/vai-tro";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        var role = roleService.findById(id);
        if (role == null) {
            ra.addFlashAttribute("errorMsg", "Không tìm thấy vai trò");
            return "redirect:/admin/vai-tro";
        }
        model.addAttribute("title", "vai-tro");
        model.addAttribute("role", role);
        model.addAttribute("groupedPermissions", roleService.getPermissionsGroupedByModule());
        return "view/admin/role/role-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_UPDATE)")
    public String edit(@PathVariable Integer id,
                       @RequestParam String name,
                       @RequestParam(required = false) String moTa,
                       @RequestParam(required = false) List<Integer> permissionIds,
                       RedirectAttributes ra) {
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Tên vai trò không được để trống");
            return "redirect:/admin/vai-tro/sua/" + id;
        }
        var oldRole = roleService.findById(id);
        try {
            roleService.save(id, name.trim().toUpperCase(), moTa, permissionIds);
            var admin = securityUtil.getCurrentUser();
            if (admin != null && oldRole != null) {
                adminLogService.ghiLog(admin, "SUA_ROLE", "ROLE", id,
                        oldRole.getName(), name.trim().toUpperCase(),
                        "Cập nhật vai trò " + oldRole.getName() + " → " + name.trim().toUpperCase());
            }
            ra.addFlashAttribute("successMsg", "Cập nhật vai trò thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vai-tro";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        var oldRole = roleService.findById(id);
        if (roleService.delete(id)) {
            var admin = securityUtil.getCurrentUser();
            if (admin != null && oldRole != null) {
                adminLogService.ghiLog(admin, "XOA_ROLE", "ROLE", id,
                        oldRole.getName(), null, "Xóa vai trò " + oldRole.getName());
            }
            ra.addFlashAttribute("successMsg", "Xóa vai trò thành công");
        } else {
            ra.addFlashAttribute("errorMsg", "Không thể xóa vai trò mặc định hoặc không tìm thấy");
        }
        return "redirect:/admin/vai-tro";
    }
}
