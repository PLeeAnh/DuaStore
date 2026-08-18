package com.duastore.config.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.duastore.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Xu ly ket noi WebSocket /ws/admin/notifications cho trang admin.
 *
 * Khi co thong bao nhan vien moi (notifyStaff), server PUSH ngay ve cho tat ca admin
 * dang mo trang quan tri — khong can cho chu ky poll 30s cua client nua.
 */
@Component
public class AdminNotificationSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationSocketHandler.class);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm, dd/MM");

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WS admin connected: {} (total={})", safeUser(session), sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WS admin closed: {} (total={})", safeUser(session), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Chi nhan de giu ket noi song; khong can xu ly noi dung.
    }

    /** Push notification moi toi TAT CA admin dang ket noi. */
    public void push(Notification n) {
        if (n == null || sessions.isEmpty()) {
            return;
        }
        String json = toJson(n);
        if (json == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                log.debug("WS push that bai cho 1 session: {}", e.getMessage());
            }
        }
    }

    /** Gui ping dinh ky de phat hien ket noi da gay (nuoc da kho), tranh session chet mem. */
    @Scheduled(fixedDelay = 60_000L)
    public void heartbeat() {
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("{\"type\":\"PING\"}"));
                }
            } catch (Exception e) {
                try { session.close(CloseStatus.SESSION_NOT_RELIABLE); } catch (Exception ignored) { }
                sessions.remove(session);
            }
        }
    }

    public int getSessionCount() {
        return sessions.size();
    }

    private String toJson(Notification n) {
        try {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "ADMIN_NOTIFICATION");
            m.put("id", n.getId());
            m.put("content", n.getContent());
            m.put("linkUrl", n.getLinkUrl());
            m.put("linkLabel", n.getLinkLabel());
            m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().format(TIME_FMT) : null);
            return objectMapper.writeValueAsString(m);
        } catch (Exception e) {
            log.warn("Serialize notification WS that bai: {}", e.getMessage());
            return null;
        }
    }

    private String safeUser(WebSocketSession session) {
        return session.getPrincipal() != null ? session.getPrincipal().getName() : "anonymous";
    }
}