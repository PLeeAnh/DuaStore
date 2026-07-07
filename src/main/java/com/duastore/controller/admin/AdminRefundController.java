package com.duastore.controller.admin;

import com.duastore.model.RefundRequest;
import com.duastore.service.admin.RefundService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/hoan-tien")
public class AdminRefundController {

    private final RefundService refundService;

    public AdminRefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_READ)")
    public String list(Model model) {
        model.addAttribute("refunds", refundService.getAll());
        model.addAttribute("pendingCount", refundService.getPendingCount());
        model.addAttribute("title", "hoan-tien");
        model.addAttribute("orderTab", "hoan-tien");
        return "view/admin/refund/list";
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_READ)")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("refund", refundService.getById(id));
        return "view/admin/refund/detail";
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_UPDATE)")
    public String approve(@PathVariable Integer id,
                          @RequestParam(required = false) String ghiChu,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes ra) {
        try {
            Integer adminId = Integer.parseInt(userDetails.getUsername());
            refundService.approve(id, adminId, ghiChu);
            ra.addFlashAttribute("successMsg", "Đã duyệt yêu cầu hoàn tiền");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_UPDATE)")
    public String reject(@PathVariable Integer id,
                         @RequestParam(required = false) String ghiChu,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes ra) {
        try {
            Integer adminId = Integer.parseInt(userDetails.getUsername());
            refundService.reject(id, adminId, ghiChu);
            ra.addFlashAttribute("successMsg", "Đã từ chối yêu cầu hoàn tiền");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }
}
