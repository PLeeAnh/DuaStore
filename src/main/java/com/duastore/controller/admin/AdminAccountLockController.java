package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.AccountLockRequest;
import com.duastore.model.User;
import com.duastore.repository.UserRepository;
import com.duastore.service.admin.AccountLockService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/yeu-cau-khoa-tk")
/**
 * phía quản trị (admin) — Product Owner duyệt/từ chối các yêu cầu khóa tài khoản
 * khách hàng do ADMIN/STAFF tạo ra.
 */
public class AdminAccountLockController {

    private final AccountLockService accountLockService;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    public AdminAccountLockController(AccountLockService accountLockService,
            UserRepository userRepository,
            SecurityUtil securityUtil) {
        this.accountLockService = accountLockService;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public String list(Model model) {
        List<AccountLockRequest> pending = accountLockService.getPending();

        Map<Integer, User> userById = new HashMap<>();
        for (AccountLockRequest r : pending) {
            userById.computeIfAbsent(r.getUserId(), id -> userRepository.findById(id).orElse(null));
            userById.computeIfAbsent(r.getRequestedBy(), id -> userRepository.findById(id).orElse(null));
        }

        model.addAttribute("title", "yeu-cau-khoa-tk");
        model.addAttribute("requests", pending);
        model.addAttribute("userById", userById);
        return "view/admin/customer/lock-requests";
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public String approve(@PathVariable Integer id,
            @RequestParam(required = false) String note,
            RedirectAttributes ra) {
        try {
            accountLockService.approve(id, securityUtil.getCurrentUser(), note);
            ra.addFlashAttribute("successMsg", "Đã duyệt và khóa tài khoản");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/yeu-cau-khoa-tk";
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('PRODUCT_OWNER')")
    public String reject(@PathVariable Integer id,
            @RequestParam(required = false) String note,
            RedirectAttributes ra) {
        try {
            accountLockService.reject(id, securityUtil.getCurrentUser(), note);
            ra.addFlashAttribute("successMsg", "Đã từ chối yêu cầu khóa");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/yeu-cau-khoa-tk";
    }
}
