package com.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataProviderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateDataProvider() throws Exception {
        String body = """
            {
              "providerCode": "API_TEST",
              "providerName": "API测试",
              "providerType": "HTTP",
              "configJson": {
                "url": "http://api.example.com/suppliers"
              }
            }
            """;

        mockMvc.perform(post("/api/data-providers")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.providerCode").value("API_TEST"));
    }

    @Test
    void testGetById() throws Exception {
        String body = """
            {
              "providerCode": "GET_API_TEST",
              "providerName": "查询API测试",
              "providerType": "STATIC"
            }
            """;

        mockMvc.perform(post("/api/data-providers")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/data-providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testList() throws Exception {
        mockMvc.perform(get("/api/data-providers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}