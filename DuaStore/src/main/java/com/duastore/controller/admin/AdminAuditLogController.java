package com.duastore.controller.admin;

import com.duastore.model.AdminActionLog;
import com.duastore.service.admin.AdminLogService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/admin/nhat-ky")
public class AdminAuditLogController {

    private final AdminLogService adminLogService;

    public AdminAuditLogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).AUDIT_LOG_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<AdminActionLog> logPage = adminLogService.getAllLogs(page, size);
        model.addAttribute("title", "nhat-ky");
        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalItems", logPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "bản ghi");
        model.addAttribute("url", "/admin/nhat-ky");
        model.addAttribute("filterParams", Map.of());
        return "view/admin/audit-log/audit-log-list";
    }
}
