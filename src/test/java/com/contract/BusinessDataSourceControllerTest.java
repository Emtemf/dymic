package com.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BusinessDataSourceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBusinessStaticShouldReturnCreatedProvider() throws Exception {
        String body = """
            {
              "providerName": "城市选择",
              "providerType": "STATIC",
              "configJson": "[{\\\"value\\\":\\\"BJ\\\",\\\"label\\\":\\\"北京\\\"}]"
            }
            """;

        mockMvc.perform(post("/api/v2/config/business-data-sources")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.providerType").value("STATIC"))
            .andExpect(jsonPath("$.data.dataSourceCategory").value("BUSINESS"));
    }

    @Test
    void listBusinessConfigsShouldReturnSuccess() throws Exception {
        mockMvc.perform(get("/api/v2/config/business-data-sources"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }
}
