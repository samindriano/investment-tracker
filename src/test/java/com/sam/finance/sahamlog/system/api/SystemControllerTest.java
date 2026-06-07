package com.sam.finance.sahamlog.system.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingEndpointShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/system/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.service").value("sahamlog"));
    }

    @Test
    void apiDocsShouldBePubliclyAccessibleInTestProfile() throws Exception {
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").exists());
    }
}
