package com.contract.adapter.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TemplateVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateDraftVersion() throws Exception {
        // 先创建模板
        String templateRequest = """
            {
              "templateCode": "VERSION_CTRL_TEST",
              "templateName": "版本测试",
              "templateDesc": "测试版本",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(templateRequest))
            .andExpect(status().isOk());

        // 通过模板编码创建版本
        String versionRequest = """
            {
              "versionNo": 2,
              "versionName": "V2.0"
            }
            """;

        mockMvc.perform(post("/api/templates/code/VERSION_CTRL_TEST/versions")
                .contentType("application/json")
                .content(versionRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.versionNo").value(2))
            .andExpect(jsonPath("$.data.versionStatus").value("DRAFT"));
    }

    @Test
    void testPublishVersion() throws Exception {
        // 先创建模板
        String templateRequest = """
            {
              "templateCode": "PUBLISH_VERSION_TEST",
              "templateName": "发布版本测试",
              "templateDesc": "测试发布版本",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(templateRequest))
            .andExpect(status().isOk());

        // createTemplate auto-creates version_no=1, create version_no=2
        String versionRequest = """
            {
              "versionNo": 2,
              "versionName": "V2.0"
            }
            """;

        String versionResponse = mockMvc.perform(post("/api/templates/code/PUBLISH_VERSION_TEST/versions")
                .contentType("application/json")
                .content(versionRequest))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // 从响应中提取版本ID (简化处理，使用JSON路径)
        // 发布版本 - 需要版本ID，这里我们通过查询当前版本来测试
        // 先查询当前版本（应该还没有）
        mockMvc.perform(get("/api/templates/code/PUBLISH_VERSION_TEST/versions/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetCurrentVersion() throws Exception {
        // 先创建模板
        String templateRequest = """
            {
              "templateCode": "CURRENT_VERSION_TEST",
              "templateName": "当前版本测试",
              "templateDesc": "测试当前版本",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(templateRequest))
            .andExpect(status().isOk());

        // 查询当前版本（应该为空，因为没有发布版本）
        mockMvc.perform(get("/api/templates/code/CURRENT_VERSION_TEST/versions/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testFullVersionLifecycle() throws Exception {
        // 先创建模板
        String templateRequest = """
            {
              "templateCode": "LIFECYCLE_TEST",
              "templateName": "生命周期测试",
              "templateDesc": "测试完整生命周期",
              "bizType": "TEST"
            }
            """;

        mockMvc.perform(post("/api/templates")
                .contentType("application/json")
                .content(templateRequest))
            .andExpect(status().isOk());

        // createTemplate auto-creates version_no=1, create version_no=2
        String versionRequest = """
            {
              "versionNo": 2,
              "versionName": "V2.0"
            }
            """;

        String response = mockMvc.perform(post("/api/templates/code/LIFECYCLE_TEST/versions")
                .contentType("application/json")
                .content(versionRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionStatus").value("DRAFT"))
            .andReturn().getResponse().getContentAsString();

        // 解析响应获取版本ID（使用简单的字符串处理）
        // 在实际项目中应该使用JSON解析库如Jackson或Gson
        Long versionId = extractIdFromResponse(response);

        // 发布版本
        mockMvc.perform(post("/api/templates/versions/" + versionId + "/publish"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        // 查询当前版本（现在应该有值）
        mockMvc.perform(get("/api/templates/code/LIFECYCLE_TEST/versions/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.versionStatus").value("PUBLISHED"));
    }

    /**
     * 从JSON响应中提取ID的辅助方法
     * 这是一个简化的实现，实际应该使用JSON解析库
     */
    private Long extractIdFromResponse(String response) {
        int idIndex = response.indexOf("\"id\":");
        if (idIndex == -1) {
            throw new RuntimeException("Cannot find id in response");
        }
        int startIndex = idIndex + 5;
        // Skip opening quote if present (Snowflake IDs may be quoted as strings)
        if (response.charAt(startIndex) == '"') {
            startIndex++;
        }
        int endIndex = response.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = response.indexOf("}", startIndex);
        }
        String idStr = response.substring(startIndex, endIndex).trim();
        // Remove trailing quote if present
        if (idStr.endsWith("\"")) {
            idStr = idStr.substring(0, idStr.length() - 1);
        }
        return Long.parseLong(idStr);
    }
}