package com.duastore.service;

import com.duastore.config.security.PermissionEnum;
import com.duastore.config.security.SecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityServiceTest {

    private final SecurityService securityService = new SecurityService();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void hasPermission_USER_withRoleUser_returnsFalse() {
        SecurityContextHolder.getContext().setAuthentication(
                auth("testuser", "ROLE_USER", List.of("USER_READ"))
        );
        assertThat(securityService.hasPermission(PermissionEnum.PRODUCT_READ)).isFalse();
    }

    @Test
    void hasPermission_ADMIN_withProductRead_returnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(
                auth("admin", "ROLE_ADMIN", List.of("PRODUCT_READ"))
        );
        assertThat(securityService.hasPermission(PermissionEnum.PRODUCT_READ)).isTrue();
    }

    @Test
    void hasPermission_SUPER_ADMIN_anyPermission_returnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(
                auth("super", "ROLE_SUPER_ADMIN", List.of())
        );
        assertThat(securityService.hasPermission("ANYTHING")).isTrue();
    }

    @Test
    void hasPermission_notAuthenticated_returnsFalse() {
        SecurityContextHolder.getContext().setAuthentication(null);
        assertThat(securityService.hasPermission(PermissionEnum.USER_READ)).isFalse();
    }

    @Test
    void hasRole_withMatchingRole_returnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(
                auth("admin", "ROLE_ADMIN", List.of())
        );
        assertThat(securityService.hasRole("ADMIN")).isTrue();
    }

    private Authentication auth(String name, String role, List<String> permissions) {
        var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return new UsernamePasswordAuthenticationToken(name, null, authorities);
    }
}
