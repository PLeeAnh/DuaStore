package com.duastore.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("sec")
/**
 * Service chứa nghiệp vụ (business logic) xử lý bảo mật/phân quyền.
 */
public class SecurityService {

    public boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_PRODUCT_OWNER".equals(a.getAuthority())) {
                return true;
            }
        }

        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a.getAuthority().equals(permission)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasRole(String roleName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        String target = "ROLE_" + roleName;
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (target.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiem tra nguoi dang dang nhap co duoc thay 1 thong bao noi bo hay khong, dua tren
     * Notification.requiredPermission — null = ai cung thay; tien to "ROLE:" = phai dung
     * vai tro do (dung cho nghiep vu rieng cua 1 vai tro, khong gan permission module nao,
     * vd duyet yeu cau khoa tai khoan chi PRODUCT_OWNER duoc lam); con lai la 1 quyen
     * trong PermissionEnum, dung lai chinh hasPermission() da co (PRODUCT_OWNER luon
     * thay tat ca).
     */
    public boolean canSeeNotification(String requiredPermission) {
        if (requiredPermission == null || requiredPermission.isBlank()) {
            return true;
        }
        if (requiredPermission.startsWith("ROLE:")) {
            return hasRole(requiredPermission.substring(5));
        }
        return hasPermission(requiredPermission);
    }
}
