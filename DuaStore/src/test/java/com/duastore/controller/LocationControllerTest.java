package com.duastore.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class LocationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void getProvinces_returns200() throws Exception {
        mockMvc.perform(get("/api/location/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDistricts_withProvinceCode_returns200() throws Exception {
        mockMvc.perform(get("/api/location/districts?provinceCode=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getDistricts_invalidProvinceCode_returns200empty() throws Exception {
        mockMvc.perform(get("/api/location/districts?provinceCode=INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getWards_withDistrictCode_returns200() throws Exception {
        mockMvc.perform(get("/api/location/wards?districtCode=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getWards_invalidDistrictCode_returns200empty() throws Exception {
        mockMvc.perform(get("/api/location/wards?districtCode=INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
