package com.duastore.controller;

import com.duastore.service.EmailService;
import com.duastore.service.VerificationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class VerificationCodeControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void sendCode_emptyEmail_returns400() throws Exception {
        String body = "{\"email\":\"\"}";
        mockMvc.perform(post("/api/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email không được bỏ trống"));
    }

    @Test
    void sendCode_validEmail_returns200() throws Exception {
        when(emailService.sendOtpEmail(anyString(), anyString(), anyString())).thenReturn(true);

        String body = "{\"email\":\"test@example.com\"}";
        mockMvc.perform(post("/api/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void sendCode_emailServiceFails_returns200WithError() throws Exception {
        when(emailService.sendOtpEmail(anyString(), anyString(), anyString())).thenReturn(false);

        String body = "{\"email\":\"test@example.com\"}";
        mockMvc.perform(post("/api/auth/send-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Không gửi được email mã xác thực. Vui lòng thử lại sau."));
    }

    @Test
    void verifyCode_nullEmail_returns400() throws Exception {
        String body = "{\"code\":\"123456\"}";
        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Thiếu thông tin"));
    }

    @Test
    void verifyCode_validCode_returns200() throws Exception {
        String email = "verify-test@example.com";
        String code = verificationCodeService.generate(email);

        String body = "{\"email\":\"" + email + "\",\"code\":\"" + code + "\"}";
        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void verifyCode_invalidCode_returns400() throws Exception {
        String body = "{\"email\":\"wrong@example.com\",\"code\":\"000000\"}";
        mockMvc.perform(post("/api/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Mã xác thực không đúng hoặc đã hết hạn"));
    }
}
