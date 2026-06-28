package com.duastore.service;

import com.duastore.model.Notification;
import com.duastore.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationHelper {

    private final NotificationRepository notificationRepository;

    public NotificationHelper(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(NotificationHelper.class);

    public void notifyAll(String content, String linkType, Integer linkId, String linkUrl, String linkLabel) {
        try {
            Notification n = new Notification();
            n.setContent(content);
            n.setLinkType(linkType);
            n.setLinkId(linkId);
            n.setLinkUrl(linkUrl);
            n.setLinkLabel(linkLabel);
            n.setIsActive(true);
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("notifyAll failed: {}", e.getMessage(), e);
        }
    }

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
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("notifyStaff failed: {}", e.getMessage(), e);
        }
    }
}
