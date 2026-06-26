package com.duastore.controller.admin;

import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(basePackages = "com.duastore.controller.admin")
public class AdminNavbarAdvice {

    private static final Logger log = LoggerFactory.getLogger(AdminNavbarAdvice.class);

    private final NotificationRepository notificationRepository;

    public AdminNavbarAdvice(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute
    public void addStaffNotifications(Model model, HttpSession session) {
        try {
            Integer readMaxId = (Integer) session.getAttribute("staffNotifReadMaxId");
            List<Notification> allStaffNotifs = notificationRepository.findStaffNotifications();

            if (readMaxId != null && readMaxId > 0) {
                List<Notification> unread = allStaffNotifs.stream()
                    .filter(n -> n.getId() > readMaxId)
                    .toList();
                model.addAttribute("staffNotifs", unread);
                model.addAttribute("staffNotifCount", notificationRepository.countUnreadStaffNotifications(readMaxId));
            } else {
                model.addAttribute("staffNotifs", allStaffNotifs);
                model.addAttribute("staffNotifCount", notificationRepository.countStaffNotifications());
            }
        } catch (Exception e) {
            log.warn("Loi lay staff notification: {}", e.getMessage());
            model.addAttribute("staffNotifs", java.util.List.of());
            model.addAttribute("staffNotifCount", 0L);
        }
    }
}
