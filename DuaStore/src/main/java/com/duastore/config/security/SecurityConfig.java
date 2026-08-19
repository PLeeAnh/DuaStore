package com.duastore.config.security;

import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final CustomOAuth2UserService oAuth2UserService;
    private final TwoFactorAuthFilter twoFactorAuthFilter;

    @Value("${duastore.remember-me.key}")
    private String rememberMeKey;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
            CustomAuthenticationSuccessHandler successHandler,
            CustomAuthenticationFailureHandler failureHandler,
            CustomOAuth2UserService oAuth2UserService,
            TwoFactorAuthFilter twoFactorAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.oAuth2UserService = oAuth2UserService;
        this.twoFactorAuthFilter = twoFactorAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/ws/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/wishlist/**", "/api/cart/**", "/address/api/**", "/api/vi-voucher/**", "/api/thong-bao/**", "/api/coupon/**").authenticated()
                .requestMatchers("/checkout/vnpay/**").permitAll()
                .requestMatchers("/gio-hang", "/checkout/**", "/tai-khoan/**", "/don-hang/**", "/wishlist/**").authenticated()
                .anyRequest().permitAll()
                )
                .formLogin(login -> login
                .loginPage("/dang-nhap")
                .loginProcessingUrl("/dang-nhap")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
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
                .ignoringRequestMatchers(
                        "/api/auth/send-code",
                        "/api/auth/verify-code",
                        "/admin/thong-bao/api/don-hang-moi",
                        "/admin/thong-bao/api/lien-he-moi",
                        "/checkout/vnpay/ipn"
                ));

        http.headers(headers -> headers
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' https://cdn.jsdelivr.net https://code.jquery.com https://unpkg.com 'unsafe-inline'; style-src 'self' https://cdn.jsdelivr.net https://unpkg.com https://fonts.googleapis.com 'unsafe-inline'; img-src 'self' data: https: blob:; font-src 'self' data: https://cdn.jsdelivr.net https://fonts.gstatic.com; connect-src 'self' https://api.ghn.vn https://cdn.jsdelivr.net https://unpkg.com https://nominatim.openstreetmap.org; frame-src 'self' https://www.google.com;"))
                );

        http.addFilterBefore(rateLimitingFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(twoFactorAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter();
    }

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingRegistration(RateLimitingFilter filter) {
        FilterRegistrationBean<RateLimitingFilter> reg = new FilterRegistrationBean<>(filter);
        // KHONG TAT filter: chi chan dang ky trung qua servlet container.
        // Filter thuc su chay trong security chain (http.addFilterBefore).
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<TwoFactorAuthFilter> twoFactorAuthRegistration(TwoFactorAuthFilter filter) {
        FilterRegistrationBean<TwoFactorAuthFilter> reg = new FilterRegistrationBean<>(filter);
        // KHONG TAT filter: chi chan dang ky trung qua servlet container.
        // Filter thuc su chay trong security chain (http.addFilterAfter).
        reg.setEnabled(false);
        return reg;
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
                        String uri = request.getRequestURI();
                        if (uri.startsWith("/api/") || uri.startsWith("/address/api/")) {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
