package com.duastore.config;

import com.duastore.config.security.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
/**
 * Lớp cấu hình Spring liên quan tới auditor config.
 */
public class AuditorConfig {

    @Bean
    public AuditorAware<Integer> auditorAware(SecurityUtil securityUtil) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            return securityUtil.getCurrentUserIdOptional();
        };
    }
}