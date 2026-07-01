package com.duastore.controller.client;

import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @GetMapping("/api/thong-bao")
    @ResponseBody
    public Map<String, Object> getNotifs(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        try {
            Integer readMaxId = (Integer) session.getAttribute("notifReadMaxId");
            @SuppressWarnings("unchecked")
            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("notifReadIds");
            final Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : new HashSet<>();

            Integer maxId = readMaxId != null ? readMaxId : 0;
            List<Notification> all = notificationRepository.findCustomerNotifications();
            List<Map<String, Object>> items = new ArrayList<>();
            for (Notification n : all) {
                boolean read = (n.getId() <= maxId) || readIds.contains(n.getId());
                Map<String, Object> item = new HashMap<>();
                item.put("id", n.getId());
                item.put("content", n.getContent());
                item.put("linkType", n.getLinkType());
                item.put("linkUrl", n.getLinkUrl());
                item.put("linkLabel", n.getLinkLabel());
                item.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
                item.put("read", read);
                items.add(item);
            }
            long unread = all.stream().filter(n -> {
                boolean r = (n.getId() <= maxId) || readIds.contains(n.getId());
                return !r;
            }).count();
            res.put("count", unread);
            res.put("notifications", items);
        } catch (Exception e) {
            res.put("count", 0);
            res.put("notifications", java.util.List.of());
        }
        return res;
    }

    @PostMapping("/api/thong-bao/doc/{id}")
    @ResponseBody
    public String markRead(@PathVariable Integer id, HttpSession session) {
        try {
            @SuppressWarnings("unchecked")
            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("notifReadIds");
            Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : new HashSet<>();
            readIds.add(id);
            session.setAttribute("notifReadIds", readIds);
        } catch (Exception ignored) {}
        return "ok";
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
