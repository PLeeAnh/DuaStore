package com.duastore.config.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

/**
 * Dang ky WebSocket /ws/admin/notifications cho trang admin.
 *
 * Bao mat: chi ADMIN / SUPER_ADMIN moi duoc bat tay (handshake). Kiem tra o 2 lop:
 *  1. SecurityFilterChain: matcher "/ws/**" yeu cau role ADMIN/SUPER_ADMIN.
 *  2. HandshakeInterceptor: rank hinh lai tai thoi diem het tay (rang buoc an toan).
 */
@Configuration
@EnableWebSocket
/**
 * phía quản trị (admin) — Lớp cấu hình Spring liên quan tới thông báo.
 */
public class AdminNotificationWebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationWebSocketConfig.class);

    private final AdminNotificationSocketHandler adminNotificationSocketHandler;

    public AdminNotificationWebSocketConfig(AdminNotificationSocketHandler adminNotificationSocketHandler) {
        this.adminNotificationSocketHandler = adminNotificationSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adminNotificationSocketHandler, "/ws/admin/notifications")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authInterceptor());
    }

    private HandshakeInterceptor authInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                    WebSocketHandler wsHandler, java.util.Map<String, Object> attributes) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                    log.warn("WS handshake bi tu choi: chua dang nhap");
                    return false;
                }
                boolean admin = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(a -> "ROLE_ADMIN".equals(a) || "ROLE_SUPER_ADMIN".equals(a));
                if (!admin) {
                    log.warn("WS handshake bi tu choi: user {} khong phai admin", auth.getName());
                }
                return admin;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                    WebSocketHandler wsHandler, Exception exception) {
                // Khong can lam gi them.
            }
        };
    }
}