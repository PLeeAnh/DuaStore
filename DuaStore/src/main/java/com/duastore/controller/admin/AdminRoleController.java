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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        model.addAttribute("userTab", "phan-quyen");
        model.addAttribute("roles", roleService.findAll());
        model.addAttribute("entityLabel", "vai trò");
        model.addAttribute("url", "/admin/vai-tro");
        return "view/admin/role/role-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_CREATE)")
    public String createForm(Model model) {
        model.addAttribute("title", "vai-tro");
        model.addAttribute("userTab", "phan-quyen");
        model.addAttribute("role", new com.duastore.model.Role());
        model.addAttribute("groupedPermissions", roleService.getPermissionsGroupedByModule());
        model.addAttribute("moduleLabels", getModuleLabels());
        model.addAttribute("permLabels", getPermLabels());
        return "view/admin/role/role-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_CREATE)")
    public String create(@RequestParam String name,
            @RequestParam(required = false) String moTa,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) List<Integer> permissionIds,
            RedirectAttributes ra) {
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Tên vai trò không được để trống");
            return "redirect:/admin/vai-tro/them-moi";
        }
        try {
            var saved = roleService.save(null, name.trim().toUpperCase(), moTa, isActive, permissionIds);
            var admin = securityUtil.getCurrentUser();
            if (admin != null) {
                adminLogService.ghiLog(admin, "TAO_ROLE", "ROLE", saved.getId(),
                        null, saved.getName(), "Tạo vai trò " + saved.getName());
            }
            ra.addFlashAttribute("successMsg", "Thêm vai trò thành công");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
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
        model.addAttribute("userTab", "phan-quyen");
        model.addAttribute("role", role);
        model.addAttribute("groupedPermissions", roleService.getPermissionsGroupedByModule());
        model.addAttribute("moduleLabels", getModuleLabels());
        model.addAttribute("permLabels", getPermLabels());
        return "view/admin/role/role-form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).ROLE_UPDATE)")
    public String edit(@PathVariable Integer id,
            @RequestParam String name,
            @RequestParam(required = false) String moTa,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) List<Integer> permissionIds,
            RedirectAttributes ra) {
        if (name == null || name.isBlank()) {
            ra.addFlashAttribute("errorMsg", "Tên vai trò không được để trống");
            return "redirect:/admin/vai-tro/sua/" + id;
        }
        var oldRole = roleService.findById(id);
        try {
            roleService.save(id, name.trim().toUpperCase(), moTa, isActive, permissionIds);
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

    private Map<String, String> getModuleLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("DASHBOARD", "Bảng điều khiển");
        labels.put("PRODUCT", "Sản phẩm");
        labels.put("VARIANT", "Biến thể sản phẩm");
        labels.put("ORDER", "Đơn hàng");
        labels.put("USER", "Người dùng");
        labels.put("ROLE", "Vai trò");
        labels.put("CATEGORY", "Danh mục");
        labels.put("PROMOTION", "Khuyến mãi");
        labels.put("REVIEW", "Đánh giá");
        labels.put("POST", "Bài viết");
        labels.put("POST_CATEGORY", "Danh mục bài viết");
        labels.put("BANNER", "Banner");
        labels.put("NOTIFICATION", "Thông báo");
        labels.put("AUDIT_LOG", "Nhật ký hệ thống");
        labels.put("CUSTOMER", "Khách hàng");
        labels.put("HOMEPAGE", "Trang chủ");
        labels.put("APPEARANCE", "Giao diện");
        labels.put("EMAIL_SETTING", "Cài đặt email");
        labels.put("PAYMENT_SETTING", "Cài đặt thanh toán");
        labels.put("SHIPPING_SETTING", "Cài đặt vận chuyển");
        labels.put("STORE", "Cửa hàng");
        labels.put("ANALYTICS", "Phân tích");
        labels.put("FLASH_SALE", "Flash Sale");
        labels.put("REFUND", "Hoàn tiền");
        labels.put("CONTACT_MESSAGE", "Tin nhắn liên hệ");
        return labels;
    }

    private Map<String, String> getPermLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("DASHBOARD:READ", "Xem bảng điều khiển");
        labels.put("PRODUCT:CREATE", "Thêm sản phẩm");
        labels.put("PRODUCT:READ", "Xem sản phẩm");
        labels.put("PRODUCT:UPDATE", "Sửa sản phẩm");
        labels.put("PRODUCT:DELETE", "Xóa sản phẩm");
        labels.put("VARIANT:CREATE", "Thêm biến thể");
        labels.put("VARIANT:READ", "Xem biến thể");
        labels.put("VARIANT:UPDATE", "Sửa biến thể");
        labels.put("VARIANT:DELETE", "Xóa biến thể");
        labels.put("ORDER:READ", "Xem đơn hàng");
        labels.put("ORDER:UPDATE", "Cập nhật đơn hàng");
        labels.put("USER:CREATE", "Thêm người dùng");
        labels.put("USER:READ", "Xem người dùng");
        labels.put("USER:UPDATE", "Sửa người dùng");
        labels.put("ROLE:CREATE", "Thêm vai trò");
        labels.put("ROLE:READ", "Xem vai trò");
        labels.put("ROLE:UPDATE", "Sửa vai trò");
        labels.put("ROLE:DELETE", "Xóa vai trò");
        labels.put("CATEGORY:CREATE", "Thêm danh mục");
        labels.put("CATEGORY:READ", "Xem danh mục");
        labels.put("CATEGORY:UPDATE", "Sửa danh mục");
        labels.put("CATEGORY:DELETE", "Xóa danh mục");
        labels.put("PROMOTION:CREATE", "Thêm khuyến mãi");
        labels.put("PROMOTION:READ", "Xem khuyến mãi");
        labels.put("PROMOTION:UPDATE", "Sửa khuyến mãi");
        labels.put("PROMOTION:DELETE", "Xóa khuyến mãi");
        labels.put("REVIEW:READ", "Xem đánh giá");
        labels.put("REVIEW:APPROVE", "Duyệt đánh giá");
        labels.put("REVIEW:HIDE", "Ẩn đánh giá");
        labels.put("REVIEW:DELETE", "Xóa đánh giá");
        labels.put("POST:CREATE", "Thêm bài viết");
        labels.put("POST:READ", "Xem bài viết");
        labels.put("POST:UPDATE", "Sửa bài viết");
        labels.put("POST:DELETE", "Xóa bài viết");
        labels.put("POST_CATEGORY:CREATE", "Thêm danh mục bài viết");
        labels.put("POST_CATEGORY:READ", "Xem danh mục bài viết");
        labels.put("POST_CATEGORY:UPDATE", "Sửa danh mục bài viết");
        labels.put("POST_CATEGORY:DELETE", "Xóa danh mục bài viết");
        labels.put("BANNER:CREATE", "Thêm banner");
        labels.put("BANNER:READ", "Xem banner");
        labels.put("BANNER:UPDATE", "Sửa banner");
        labels.put("BANNER:DELETE", "Xóa banner");
        labels.put("NOTIFICATION:CREATE", "Thêm thông báo");
        labels.put("NOTIFICATION:READ", "Xem thông báo");
        labels.put("NOTIFICATION:UPDATE", "Sửa thông báo");
        labels.put("NOTIFICATION:DELETE", "Xóa thông báo");
        labels.put("AUDIT_LOG:READ", "Xem nhật ký");
        labels.put("CUSTOMER:READ", "Xem khách hàng");
        labels.put("CUSTOMER:UPDATE", "Sửa khách hàng");
        labels.put("HOMEPAGE:READ", "Xem trang chủ");
        labels.put("HOMEPAGE:UPDATE", "Sửa trang chủ");
        labels.put("APPEARANCE:READ", "Xem giao diện");
        labels.put("APPEARANCE:UPDATE", "Sửa giao diện");
        labels.put("EMAIL_SETTING:READ", "Xem cài đặt email");
        labels.put("EMAIL_SETTING:UPDATE", "Sửa cài đặt email");
        labels.put("PAYMENT_SETTING:READ", "Xem cài đặt thanh toán");
        labels.put("PAYMENT_SETTING:UPDATE", "Sửa cài đặt thanh toán");
        labels.put("SHIPPING_SETTING:READ", "Xem cài đặt vận chuyển");
        labels.put("SHIPPING_SETTING:UPDATE", "Sửa cài đặt vận chuyển");
        labels.put("STORE:READ", "Xem cửa hàng");
        labels.put("STORE:UPDATE", "Sửa cửa hàng");
        labels.put("ANALYTICS:READ", "Xem phân tích");
        labels.put("FLASH_SALE:CREATE", "Thêm flash sale");
        labels.put("FLASH_SALE:READ", "Xem flash sale");
        labels.put("FLASH_SALE:UPDATE", "Sửa flash sale");
        labels.put("FLASH_SALE:DELETE", "Xóa flash sale");
        labels.put("REFUND:READ", "Xem hoàn tiền");
        labels.put("REFUND:UPDATE", "Xử lý hoàn tiền");
        labels.put("REFUND:APPROVE", "Duyệt hoàn tiền");
        labels.put("CONTACT_MESSAGE:READ", "Xem tin nhắn liên hệ");
        labels.put("CONTACT_MESSAGE:UPDATE", "Đánh dấu / xử lý tin nhắn");
        labels.put("CONTACT_MESSAGE:DELETE", "Xóa tin nhắn");
        return labels;
    }
}
