package com.duastore.controller.admin;

import com.duastore.config.security.SecurityService;
import com.duastore.dto.AdminNotificationDTO;
import com.duastore.model.Notification;
import com.duastore.model.Product;
import com.duastore.model.Promotion;
import com.duastore.repository.NotificationRepository;
import com.duastore.service.admin.AdminNotificationService;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.PromotionRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/thong-bao")
/**
 * phía quản trị (admin) — Controller xử lý các request HTTP liên quan tới thông báo.
 */
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public AdminNotificationController(AdminNotificationService adminNotificationService,
            ProductRepository productRepository,
            PromotionRepository promotionRepository,
            NotificationRepository notificationRepository,
            SecurityService securityService) {
        this.adminNotificationService = adminNotificationService;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
        this.notificationRepository = notificationRepository;
        this.securityService = securityService;
    }

    @GetMapping
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_READ)")
    public String list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<Notification> notifPage = adminNotificationService.getAllNotifications(page, size);
        model.addAttribute("notifications", notifPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifPage.getTotalPages());
        model.addAttribute("totalItems", notifPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("entityLabel", "thông báo");
        model.addAttribute("url", "/admin/thong-bao");
        model.addAttribute("filterParams", new HashMap<>());
        model.addAttribute("title", "thong-bao");
        return "view/admin/notification/notification-list";
    }

    @GetMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_CREATE)")
    public String createForm(Model model) {
        List<Product> products = productRepository.findByIsActiveTrueOrderByNgayTaoDesc();
        List<Promotion> promotions = promotionRepository.findAll();

        model.addAttribute("notif", new AdminNotificationDTO());
        model.addAttribute("formAction", "/admin/thong-bao/them-moi");
        model.addAttribute("products", products);
        model.addAttribute("promotions", promotions);
        model.addAttribute("title", "thong-bao");
        return "view/admin/notification/notification-form";
    }

    @PostMapping("/them-moi")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_CREATE)")
    public String create(@Valid @ModelAttribute("notif") AdminNotificationDTO dto,
            BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("promotions", promotionRepository.findAll());
            model.addAttribute("formAction", "/admin/thong-bao/them-moi");
            model.addAttribute("title", "thong-bao");
            return "view/admin/notification/notification-form";
        }
        try {
            adminNotificationService.save(dto);
            ra.addFlashAttribute("successMsg", "Đăng thông báo thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/thong-bao";
    }

    @GetMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_UPDATE)")
    public String editForm(@PathVariable Integer id, Model model) {
        try {
            Notification notification = adminNotificationService.getNotificationById(id);
            AdminNotificationDTO dto = new AdminNotificationDTO();
            dto.setId(notification.getId());
            dto.setContent(notification.getContent());
            if ("PRODUCT".equals(notification.getLinkType()) && notification.getLinkId() != null) {
                dto.setProductId(notification.getLinkId());
            } else if ("PROMOTION".equals(notification.getLinkType()) && notification.getLinkId() != null) {
                dto.setPromotionId(notification.getLinkId());
            }

            model.addAttribute("notif", dto);
            model.addAttribute("formAction", "/admin/thong-bao/sua/" + id);
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("promotions", promotionRepository.findAll());
            model.addAttribute("title", "thong-bao");
            return "view/admin/notification/notification-form";
        } catch (Exception e) {
            return "redirect:/admin/thong-bao";
        }
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_UPDATE)")
    public String edit(@PathVariable Integer id,
            @Valid @ModelAttribute("notif") AdminNotificationDTO dto,
            BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("products", productRepository.findByIsActiveTrueOrderByNgayTaoDesc());
            model.addAttribute("promotions", promotionRepository.findAll());
            model.addAttribute("formAction", "/admin/thong-bao/sua/" + id);
            model.addAttribute("title", "thong-bao");
            return "view/admin/notification/notification-form";
        }
        try {
            dto.setId(id);
            adminNotificationService.save(dto);
            ra.addFlashAttribute("successMsg", "Cập nhật thông báo thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/thong-bao";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_DELETE)")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            adminNotificationService.deleteNotification(id);
            ra.addFlashAttribute("successMsg", "Xóa thông báo thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/thong-bao";
    }

    @GetMapping("/api/count")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_READ)")
    public String countUnread(HttpSession session) {
        try {
            Integer readMaxId = (Integer) session.getAttribute("staffNotifReadMaxId");
            if (readMaxId == null) readMaxId = 0;
            final int maxId = readMaxId;
            @SuppressWarnings("unchecked")
            java.util.Set<Integer> readIdsRaw = (java.util.Set<Integer>) session.getAttribute("staffNotifReadIds");
            java.util.Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : java.util.Set.of();
            long count = notificationRepository.findStaffNotifications().stream()
                    .filter(n -> securityService.canSeeNotification(n.getRequiredPermission()))
                    .filter(n -> n.getId() > maxId && !readIds.contains(n.getId()))
                    .count();
            return String.valueOf(count);
        } catch (Exception e) {
            return "0";
        }
    }

    @PostMapping("/api/doc-tat-ca")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_READ)")
    public String markAllStaffRead(HttpSession session) {
        session.setAttribute("staffNotifReadMaxId",
                notificationRepository.findTopByIsActiveTrueOrderByIdDesc()
                        .map(Notification::getId).orElse(0));
        return "ok";
    }

    @PostMapping("/api/doc/{id}")
    @ResponseBody
    @PreAuthorize("@sec.hasPermission(T(com.duastore.config.security.PermissionEnum).NOTIFICATION_READ)")
    public String markStaffRead(@PathVariable Integer id, HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Set<Integer> readIds = (Set<Integer>) session.getAttribute("staffNotifReadIds");
            if (readIds == null) {
                readIds = new HashSet<>();
            }
            readIds.add(id);
            session.setAttribute("staffNotifReadIds", readIds);
        } catch (Exception ignored) {
        }
        return "ok";
    }
}
