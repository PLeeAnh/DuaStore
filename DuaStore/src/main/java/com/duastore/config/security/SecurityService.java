package com.duastore.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("sec")
public class SecurityService {

    public boolean hasPermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority a : auth.getAuthorities()) {
            if ("ROLE_SUPER_ADMIN".equals(a.getAuthority())) {
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
}
