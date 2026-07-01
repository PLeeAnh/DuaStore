package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.UserVoucher;
import com.duastore.model.VoucherStatus;
import com.duastore.service.client.VoucherWalletService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
public class VoucherWalletController {

    private final VoucherWalletService voucherWalletService;
    private final SecurityUtil securityUtil;

    public VoucherWalletController(VoucherWalletService voucherWalletService,
                                   SecurityUtil securityUtil) {
        this.voucherWalletService = voucherWalletService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/vi-voucher")
    public String wallet(@RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         @RequestParam(defaultValue = "") String tab,
                         Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        Integer userId = securityUtil.getCurrentUserId();

        VoucherStatus status = parseTab(tab);
        if (status == null) status = VoucherStatus.AVAILABLE;

        Page<UserVoucher> voucherPage = voucherWalletService.getWalletByTab(userId, status, page, size);

        model.addAttribute("vouchers", voucherPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", voucherPage.getTotalPages());
        model.addAttribute("totalItems", voucherPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("tab", tab);
        model.addAttribute("availableCount", voucherWalletService.countAvailable(userId));
        model.addAttribute("title", "vi-voucher");
        return "view/client/voucher/wallet";
    }

    @PostMapping("/api/vi-voucher/luu/{promotionId}")
    @ResponseBody
    public Map<String, Object> saveVoucher(@PathVariable Integer promotionId,
                                            Principal principal) {
        if (principal == null) {
            return Map.of("success", false, "message", "Vui lòng đăng nhập");
        }
        try {
            Integer userId = securityUtil.getCurrentUserId();
            voucherWalletService.saveVoucher(userId, promotionId);
            return Map.of("success", true, "message", "Đã lưu voucher vào ví");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/api/vi-voucher/xoa/{voucherId}")
    @ResponseBody
    public Map<String, Object> removeVoucher(@PathVariable Integer voucherId,
                                              Principal principal) {
        if (principal == null) {
            return Map.of("success", false, "message", "Vui lòng đăng nhập");
        }
        try {
            Integer userId = securityUtil.getCurrentUserId();
            voucherWalletService.removeVoucher(userId, voucherId);
            return Map.of("success", true, "message", "Đã xóa voucher");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    private VoucherStatus parseTab(String tab) {
        return switch (tab.toLowerCase()) {
            case "available" -> VoucherStatus.AVAILABLE;
            case "used" -> VoucherStatus.USED;
            case "expired" -> VoucherStatus.EXPIRED;
            default -> null;
        };
    }
}
