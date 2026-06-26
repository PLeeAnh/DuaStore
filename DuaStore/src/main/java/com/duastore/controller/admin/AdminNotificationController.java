package com.duastore.controller.admin;

import com.duastore.dto.AdminNotificationDTO;
import com.duastore.model.Notification;
import com.duastore.model.Product;
import com.duastore.model.Promotion;
import com.duastore.service.admin.AdminNotificationService;
import com.duastore.repository.ProductRepository;
import com.duastore.repository.PromotionRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/admin/thong-bao")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;

    public AdminNotificationController(AdminNotificationService adminNotificationService,
                                       ProductRepository productRepository,
                                       PromotionRepository promotionRepository) {
        this.adminNotificationService = adminNotificationService;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
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
}
