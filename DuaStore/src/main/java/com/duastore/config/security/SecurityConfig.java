package com.duastore.config.security;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService oAuth2UserService;

    @Value("${duastore.remember-me.key}")
    private String rememberMeKey;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
            CustomAuthenticationSuccessHandler successHandler,
            CustomOAuth2UserService oAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.oAuth2UserService = oAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/wishlist/**", "/api/cart/**").authenticated()
                .requestMatchers("/gio-hang", "/checkout/**", "/tai-khoan/**", "/don-hang/**", "/wishlist/**").authenticated()
                .anyRequest().permitAll()
                )
                .formLogin(login -> login
                .loginPage("/dang-nhap")
                .loginProcessingUrl("/dang-nhap")
                .successHandler(successHandler)
                .failureUrl("/?loginError=true")
                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/dang-nhap")
                .userInfoEndpoint(userInfo -> userInfo
                .userService(oAuth2UserService)
                )
                .successHandler(successHandler)
                .failureUrl("/?loginError=true")
                )
                .logout(logout -> logout
                .logoutUrl("/dang-xuat")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
                )
                .rememberMe(remember -> remember
                .rememberMeServices(rememberMeServices())
                .key(rememberMeKey)
                )
                .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .expiredUrl("/dang-nhap?expired=true")
                )
                .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint())
                )
                .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/**", "/admin/thong-bao/api/**", "/api/thong-bao/**", "/api/cart/**")
                );

        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException authException) throws IOException {
                if (request.getRequestURI().startsWith("/api/")) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
                } else {
                    response.sendRedirect(request.getContextPath() + "/dang-nhap");
                }
            }
        };
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rm = new TokenBasedRememberMeServices(
                rememberMeKey, userDetailsService);
        rm.setParameter("remember-me");
        rm.setTokenValiditySeconds(1209600);
        return rm;
    }
}
