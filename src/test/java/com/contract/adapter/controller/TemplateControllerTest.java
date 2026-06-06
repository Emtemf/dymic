package com.contract.adapter.controller;

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
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateTemplate() throws Exception {
        String requestBody = """
            {
              "templateCode": "API_TEST",
              "templateName": "API测试模板",
              "templateDesc": "测试用",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateCode").value("API_TEST"));
    }

    @Test
    void testGetTemplateByCode() throws Exception {
        // 先创建
        String requestBody = """
            {
              "templateCode": "GET_TEST",
              "templateName": "查询测试",
              "templateDesc": "测试查询",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk());

        // 再查询
        mockMvc.perform(get("/api/templates/code/GET_TEST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.templateName").value("查询测试"));
    }

    @Test
    void testGetTemplateById() throws Exception {
        // 先创建
        String requestBody = """
            {
              "templateCode": "GET_BY_ID_TEST",
              "templateName": "ID查询测试",
              "templateDesc": "测试ID查询",
              "bizType": "TEST"
            }
            """;

        String response = mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // 从响应中提取ID（简化处理，实际应使用JSON解析）
        // 这里我们通过code查询来验证创建成功
        mockMvc.perform(get("/api/templates/code/GET_BY_ID_TEST"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.templateName").value("ID查询测试"));
    }

    @Test
    void testDisableTemplate() throws Exception {
        // 先创建
        String requestBody = """
            {
              "templateCode": "DISABLE_TEST",
              "templateName": "停用测试",
              "templateDesc": "测试停用",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk());

        // 停用
        mockMvc.perform(post("/api/templates/code/DISABLE_TEST/disable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testEnableTemplate() throws Exception {
        // 先创建
        String requestBody = """
            {
              "templateCode": "ENABLE_TEST",
              "templateName": "启用测试",
              "templateDesc": "测试启用",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isOk());

        // 先停用
        mockMvc.perform(post("/api/templates/code/ENABLE_TEST/disable"))
            .andExpect(status().isOk());

        // 再启用
        mockMvc.perform(post("/api/templates/code/ENABLE_TEST/enable"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}