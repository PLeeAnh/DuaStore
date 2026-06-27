package com.duastore.controller.client;

import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/thong-bao")
    public String list(Model model, HttpSession session) {
        List<Notification> notifications = notificationRepository.findCustomerNotifications();
        model.addAttribute("notifications", notifications);

        session.setAttribute("notifReadMaxId",
            notificationRepository.findTopByIsActiveTrueOrderByIdDesc()
                .map(Notification::getId).orElse(0));

        return "view/client/notification/notification-list";
    }

    @PostMapping("/api/thong-bao/doc-tat-ca")
    @ResponseBody
    public String markAllRead(HttpSession session) {
        session.setAttribute("notifReadMaxId",
            notificationRepository.findTopByIsActiveTrueOrderByIdDesc()
                .map(Notification::getId).orElse(0));
        return "ok";
    }
}
