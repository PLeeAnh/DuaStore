package com.duastore.controller;

import com.duastore.model.Address;
import com.duastore.model.User;
import com.duastore.repository.AddressRepository;
import com.duastore.repository.UserRepository;
import com.duastore.service.GeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AddressControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeocodingService geocodingService;

    private Integer userId;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        GeocodingService geocodingService() {
            return Mockito.mock(GeocodingService.class);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        addressRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("addr-test-user");
        user.setEmail("addr-test@example.com");
        user.setPassword("pass");
        user.setHoTen("Address Test User");
        user.setIsActive(true);
        user.setNgayTao(LocalDateTime.now());
        user = userRepository.save(user);
        userId = user.getId();
    }

    @Test
    void apiSaveAddress_withoutAuth_returnsError() throws Exception {
        mockMvc.perform(post("/address/api/save")
                        .with(csrf())
                        .param("tenNguoiNhan", "Test")
                        .param("soDienThoai", "0900000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Vui lòng đăng nhập"));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiSaveAddress_success_returns200() throws Exception {
        doNothing().when(geocodingService).geocodeIfMissing(any());

        mockMvc.perform(post("/address/api/save")
                        .with(csrf())
                        .param("tenNguoiNhan", "Test User")
                        .param("soDienThoai", "0900000000")
                        .param("tinhThanh", "Hà Nội")
                        .param("quanHuyen", "Cầu Giấy")
                        .param("phuongXa", "Dịch Vọng")
                        .param("diaChiCuThe", "Số 1, Đường ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiSaveAddress_exceedsLimit_returnsError() throws Exception {
        doNothing().when(geocodingService).geocodeIfMissing(any());

        for (int i = 0; i < 10; i++) {
            Address addr = new Address();
            addr.setUserId(userId);
            addr.setTenNguoiNhan("User " + i);
            addr.setSoDienThoai("090000000" + i);
            addr.setTinhThanh("HN");
            addr.setQuanHuyen("Q" + i);
            addr.setPhuongXa("P" + i);
            addr.setDiaChiCuThe("Addr " + i);
            addressRepository.save(addr);
        }

        mockMvc.perform(post("/address/api/save")
                        .with(csrf())
                        .param("tenNguoiNhan", "Extra User")
                        .param("soDienThoai", "0900000000")
                        .param("tinhThanh", "HN")
                        .param("quanHuyen", "Q")
                        .param("phuongXa", "P")
                        .param("diaChiCuThe", "Addr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Chỉ được thêm tối đa 10 địa chỉ"));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiSetDefault_success_returns200() throws Exception {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setTenNguoiNhan("Test");
        addr.setSoDienThoai("0900000000");
        addr.setTinhThanh("HN");
        addr.setQuanHuyen("Q");
        addr.setPhuongXa("P");
        addr.setDiaChiCuThe("Addr");
        addr = addressRepository.save(addr);

        mockMvc.perform(post("/address/api/set-default/{id}", addr.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiDelete_success_returns200() throws Exception {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setTenNguoiNhan("Test");
        addr.setSoDienThoai("0900000000");
        addr.setTinhThanh("HN");
        addr.setQuanHuyen("Q");
        addr.setPhuongXa("P");
        addr.setDiaChiCuThe("Addr");
        addr = addressRepository.save(addr);

        mockMvc.perform(post("/address/api/delete/{id}", addr.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiGetAddress_success_returns200() throws Exception {
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setTenNguoiNhan("Test User");
        addr.setSoDienThoai("0900000000");
        addr.setTinhThanh("Hà Nội");
        addr.setQuanHuyen("Cầu Giấy");
        addr.setPhuongXa("Dịch Vọng");
        addr.setDiaChiCuThe("Số 1 ABC");
        addr = addressRepository.save(addr);

        mockMvc.perform(get("/address/api/{id}", addr.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tenNguoiNhan").value("Test User"))
                .andExpect(jsonPath("$.tinhThanh").value("Hà Nội"));
    }

    @Test
    @WithMockUser(username = "addr-test@example.com")
    void apiGetAddress_notFound_returnsError() throws Exception {
        mockMvc.perform(get("/address/api/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy địa chỉ"));
    }
}
