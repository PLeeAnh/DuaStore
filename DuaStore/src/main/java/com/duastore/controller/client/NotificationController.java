package com.duastore.controller.client;

import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping(value = "/api/thong-bao", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getNotificationsJson(HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();
        Integer readMaxId = (Integer) session.getAttribute("notifReadMaxId");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm, dd/MM");

        List<Map<String, String>> notifList = new ArrayList<>();

        if (readMaxId != null && readMaxId > 0) {
            List<Notification> unread = notificationRepository
                .findByIsActiveTrueAndIdGreaterThanOrderByCreatedAtDesc(readMaxId);
            for (Notification n : unread) {
                notifList.add(buildNotifMap(n, fmt));
            }
            result.put("count", notificationRepository.countUnreadCustomerNotifications(readMaxId));
        } else {
            List<Notification> all = notificationRepository.findCustomerNotifications();
            for (Notification n : all) {
                notifList.add(buildNotifMap(n, fmt));
            }
            result.put("count", notificationRepository.countCustomerNotifications());
        }

        result.put("notifications", notifList);
        return result;
    }

    private Map<String, String> buildNotifMap(Notification n, DateTimeFormatter fmt) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("id", String.valueOf(n.getId()));
        m.put("content", n.getContent());
        m.put("linkType", n.getLinkType() != null ? n.getLinkType() : "");
        m.put("linkUrl", n.getLinkUrl() != null ? n.getLinkUrl() : "");
        m.put("linkLabel", n.getLinkLabel() != null ? n.getLinkLabel() : "");
        m.put("time", n.getCreatedAt() != null ? n.getCreatedAt().format(fmt) : "");
        return m;
    }
}
