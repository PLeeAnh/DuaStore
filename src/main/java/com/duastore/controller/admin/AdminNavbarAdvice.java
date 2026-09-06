package com.duastore.controller.admin;

import com.duastore.config.security.SecurityService;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Set;

@ControllerAdvice(basePackages = "com.duastore.controller.admin")
/**
 * phía quản trị (admin) — Lớp @ControllerAdvice bổ sung dữ liệu/logic dùng chung cho các Controller liên quan tới admin navbar advice.
 */
public class AdminNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(AdminNavbarAdvice.class);

    private final NotificationRepository notificationRepository;
    private final SecurityService securityService;

    public AdminNavbarAdvice(NotificationRepository notificationRepository, SecurityService securityService) {
        this.notificationRepository = notificationRepository;
        this.securityService = securityService;
    }

    @SuppressWarnings("unchecked")
    @ModelAttribute
    public void addStaffNotifications(Model model, HttpSession session) {
        try {
            Integer readMaxId = (Integer) session.getAttribute("staffNotifReadMaxId");
            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("staffNotifReadIds");
            final Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : java.util.Collections.emptySet();
            // Chi giu lai thong bao KHOP NGHIEP VU cua vai tro dang dang nhap (xem
            // Notification.requiredPermission) — truoc day moi ADMIN/STAFF/PRODUCT_OWNER
            // deu thay chung 1 dong thong bao du khong lien quan cong viec cua ho.
            List<Notification> allStaffNotifs = notificationRepository.findStaffNotifications().stream()
                    .filter(n -> securityService.canSeeNotification(n.getRequiredPermission()))
                    .toList();

            if (readMaxId != null && readMaxId > 0) {
                List<Notification> unread = allStaffNotifs.stream()
                        .filter(n -> n.getId() > readMaxId && !readIds.contains(n.getId()))
                        .toList();
                model.addAttribute("staffNotifs", unread);
                long count = allStaffNotifs.stream()
                        .filter(n -> n.getId() > readMaxId && !readIds.contains(n.getId()))
                        .count();
                model.addAttribute("staffNotifCount", count);
            } else {
                List<Notification> unread = allStaffNotifs.stream()
                        .filter(n -> !readIds.contains(n.getId()))
                        .toList();
                model.addAttribute("staffNotifs", unread);
                model.addAttribute("staffNotifCount", (long) unread.size());
            }
        } catch (Exception e) {
            log.warn("Loi lay staff notification: {}", e.getMessage());
            model.addAttribute("staffNotifs", java.util.List.of());
            model.addAttribute("staffNotifCount", 0L);
        }
    }
}
