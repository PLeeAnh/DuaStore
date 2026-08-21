package com.duastore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
/**
 * Lớp cấu hình Spring liên quan tới cors config.
 */
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();
        String corsOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        if (corsOrigins == null || corsOrigins.isBlank()) {
            corsOrigins = "http://localhost:8080";
        }
        config.setAllowedOriginPatterns(
                Arrays.stream(corsOrigins.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
