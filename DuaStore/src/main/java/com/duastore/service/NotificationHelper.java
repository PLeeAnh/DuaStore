package com.duastore.service;

import com.duastore.config.websocket.AdminNotificationSocketHandler;
import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationHelper {

    private static final Logger log = LoggerFactory.getLogger(NotificationHelper.class);
    private final NotificationRepository notificationRepository;
    private final AdminNotificationSocketHandler adminNotificationSocketHandler;

    public NotificationHelper(NotificationRepository notificationRepository,
            AdminNotificationSocketHandler adminNotificationSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.adminNotificationSocketHandler = adminNotificationSocketHandler;
    }

    @Transactional
    public void notifyAll(String content, String linkType, Integer linkId, String linkUrl, String linkLabel) {
        notifyAll(content, linkType, linkId, linkUrl, linkLabel, null);
    }

    @Transactional
    public void notifyAll(String content, String linkType, Integer linkId, String linkUrl, String linkLabel, Integer userId) {
        try {
            Notification n = new Notification();
            n.setContent(content);
            n.setLinkType(linkType);
            n.setLinkId(linkId);
            n.setLinkUrl(linkUrl);
            n.setLinkLabel(linkLabel);
            n.setUserId(userId);
            n.setIsActive(true);
            notificationRepository.save(n);
        } catch (Exception e) {
            log.warn("Loi tao notify: {}", e.getMessage());
        }
    }

    @Transactional
    public void notifyStaff(String content, String linkType, Integer linkId, String linkUrl, String linkLabel) {
        try {
            Notification n = new Notification();
            n.setContent(content);
            n.setTargetRole("STAFF");
            n.setLinkType(linkType);
            n.setLinkId(linkId);
            n.setLinkUrl(linkUrl);
            n.setLinkLabel(linkLabel);
            n.setIsActive(true);
            Notification saved = notificationRepository.save(n);
            // PUSH realtime qua WebSocket cho cac admin dang mo trang (khong can cho poll 30s).
            try {
                adminNotificationSocketHandler.push(saved);
            } catch (Exception wsE) {
                log.debug("WS push notifyStaff khong goi duoc: {}", wsE.getMessage());
            }
        } catch (Exception e) {
            log.warn("Loi tao notifyStaff: {}", e.getMessage());
        }
    }
}
