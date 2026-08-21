package com.duastore.controller.admin;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.ProductVariant;
import com.duastore.model.RefundRequest;
import com.duastore.model.ReturnCondition;
import com.duastore.service.NotificationHelper;
import com.duastore.service.FileUploadService;
import com.duastore.service.admin.RefundService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/hoan-tien")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới hoàn trả/đổi trả đơn hàng.
 */
public class AdminRefundController {

    private final RefundService refundService;
    private final SecurityUtil securityUtil;
    private final NotificationHelper notificationHelper;
    private final FileUploadService fileUploadService;

    public AdminRefundController(RefundService refundService, SecurityUtil securityUtil,
            NotificationHelper notificationHelper, FileUploadService fileUploadService) {
        this.refundService = refundService;
        this.securityUtil = securityUtil;
        this.notificationHelper = notificationHelper;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_READ)")
    public String list(Model model) {
        List<RefundRequest> refunds = refundService.getAll();
        model.addAttribute("refunds", refunds);
        model.addAttribute("customerNames", refundService.getCustomerNames(refunds));
        model.addAttribute("pendingCount", refundService.getPendingCount());
        model.addAttribute("approvedCount", refundService.getCountByStatus("DA_DUYET"));
        model.addAttribute("completedCount", refundService.getCountByStatus("DA_HOAN_TIEN"));
        model.addAttribute("rejectedCount", refundService.getCountByStatus("TU_CHOI"));
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
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_APPROVE)")
    public String approve(@PathVariable Integer id,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            RefundRequest refund = refundService.approve(id, adminId, ghiChu);
            notifyRefundResult(refund, true);
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
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            RefundRequest refund = refundService.reject(id, adminId, ghiChu);
            notifyRefundResult(refund, false);
            ra.addFlashAttribute("successMsg", "Đã từ chối yêu cầu hoàn tiền");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }
    private void notifyRefundResult(RefundRequest refund, boolean approved) {
        String resultText = approved ? "da duoc duyet" : "da bi tu choi";
        notificationHelper.notifyAll(
                "Don " + refund.getOrderId() + ": yeu cau hoan tien " + resultText,
                "ORDER", refund.getOrderId(),
                "/tai-khoan/don-hang/" + refund.getOrderId(),
                "Xem don hang",
                refund.getUserId()
        );
        notificationHelper.notifyStaff(
                "Yeu cau hoan tien don " + refund.getOrderId() + " " + resultText,
                "ORDER", refund.getOrderId(),
                "/admin/hoan-tien/detail/" + refund.getId(),
                "Xem yeu cau"
        );
    }

    @GetMapping("/inspect/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_READ)")
    public String inspectForm(@PathVariable Integer id, Model model) {
        RefundRequest refund = refundService.getById(id);
        model.addAttribute("refund", refund);
        model.addAttribute("conditions", ReturnCondition.values());
        return "view/admin/refund/inspect";
    }

    @PostMapping("/inspect/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_APPROVE)")
    public String inspect(@PathVariable Integer id,
            @RequestParam String tinhTrangHangTra,
            @RequestParam(required = false) String ghiChu,
            @RequestParam(required = false) MultipartFile anhThucTe,
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            ReturnCondition condition = ReturnCondition.fromCode(tinhTrangHangTra);
            RefundRequest refund = refundService.processWarehouseInspection(id, condition, adminId, ghiChu);
            if (anhThucTe != null && !anhThucTe.isEmpty()) {
                String url = fileUploadService.save(anhThucTe, "refunds/inspection");
                refundService.saveActualPhoto(id, url);
            }
            ra.addFlashAttribute("successMsg", "Đã cập nhật kết quả kiểm tra: " + condition.getDisplayName());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }

    @PostMapping("/complete/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_APPROVE)")
    public String complete(@PathVariable Integer id,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            RefundRequest refund = refundService.completeRefund(id, adminId, ghiChu);
            ra.addFlashAttribute("successMsg", "Đã hoàn tiền thành công: " + refund.getSoTienThucTeHoan() + " VNĐ");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }

    @GetMapping("/exchange/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_READ)")
    public String exchangeForm(@PathVariable Integer id, Model model) {
        RefundRequest refund = refundService.getById(id);
        model.addAttribute("refund", refund);
        model.addAttribute("variants", getAvailableVariantsForExchange(refund));
        model.addAttribute("oldItemPrice", refundService.getOldItemPriceForExchange(refund.getOrderId()));
        return "view/admin/refund/exchange";
    }

    @PostMapping("/exchange/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_UPDATE)")
    public String exchange(@PathVariable Integer id,
            @RequestParam Integer variantMoiId,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            RefundRequest refund = refundService.processExchange(id, variantMoiId, adminId, ghiChu);
            ra.addFlashAttribute("successMsg", "Đã xử lý đổi hàng thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }

    @PostMapping("/update-tracking/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).REFUND_UPDATE)")
    public String updateTracking(@PathVariable Integer id,
            @RequestParam String maVanDonTra,
            RedirectAttributes ra) {
        try {
            Integer adminId = securityUtil.getCurrentUserId();
            refundService.updateReturnTracking(id, maVanDonTra, adminId);
            ra.addFlashAttribute("successMsg", "Đã cập nhật mã vận đơn trả hàng");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/hoan-tien/detail/" + id;
    }

    private List<ProductVariant> getAvailableVariantsForExchange(RefundRequest refund) {
        return refundService.getAvailableVariantsForExchange(refund.getOrderId());
    }
}
