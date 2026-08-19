package com.duastore.controller.client;

import com.duastore.config.security.SecurityUtil;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final SecurityUtil securityUtil;

    public NotificationController(NotificationRepository notificationRepository, SecurityUtil securityUtil) {
        this.notificationRepository = notificationRepository;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/thong-bao")
    public String list(Model model, HttpSession session) {
        Integer userId = securityUtil.getCurrentUserId();
        List<Notification> notifications = userId != null
                ? notificationRepository.findCustomerNotifications(userId)
                : java.util.List.of();
        model.addAttribute("notifications", notifications);

        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<Notification> newNotifs = new ArrayList<>();
        List<Notification> oldNotifs = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getCreatedAt() != null && n.getCreatedAt().isAfter(cutoff)) {
                newNotifs.add(n);
            } else {
                oldNotifs.add(n);
            }
        }
        model.addAttribute("newNotifs", newNotifs);
        model.addAttribute("oldNotifs", oldNotifs);

        return "view/client/notification/notification-list";
    }

    @GetMapping(value = "/api/thong-bao", produces = "application/json")
    @ResponseBody
    public Map<String, Object> getNotificationsJson(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId == null) {
                res.put("count", 0);
                res.put("notifications", java.util.List.of());
                return res;
            }
            @SuppressWarnings("unchecked")
            Set<Integer> readIdsRaw = (Set<Integer>) session.getAttribute("notifReadIds");
            final Set<Integer> readIds = readIdsRaw != null ? readIdsRaw : new HashSet<>();

            List<Notification> all = notificationRepository.findCustomerNotifications(userId);
            List<Map<String, Object>> items = new ArrayList<>();
            for (Notification n : all) {
                boolean read = readIds.contains(n.getId());
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
            long unread = all.stream().filter(n -> !readIds.contains(n.getId())).count();
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
        } catch (Exception ignored) {
        }
        return "ok";
    }

    @PostMapping("/api/thong-bao/xoa/{id}")
    @ResponseBody
    public String deleteNotif(@PathVariable Integer id) {
        Integer userId = securityUtil.getCurrentUserId();
        if (userId != null) {
            notificationRepository.findByIdAndUserId(id, userId).ifPresent(n -> {
                n.setIsActive(false);
                notificationRepository.save(n);
            });
        }
        return "ok";
    }

    @PostMapping("/api/thong-bao/doc-tat-ca")
    @ResponseBody
    public String markAllRead(HttpSession session) {
        try {
            Integer userId = securityUtil.getCurrentUserId();
            if (userId != null) {
                Set<Integer> readIds = new HashSet<>();
                notificationRepository.findCustomerNotifications(userId)
                        .forEach(n -> readIds.add(n.getId()));
                session.setAttribute("notifReadIds", readIds);
            }
        } catch (Exception ignored) {
        }
        return "ok";
    }
}
