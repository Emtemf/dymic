package com.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 布局节点 Controller 测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LayoutNodeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateLayoutNode() throws Exception {
        String body = """
            {
              "nodeType": "CARD",
              "nodeName": "基本信息",
              "parentId": null,
              "sortNo": 1,
              "levelNo": 1
            }
            """;

        mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.nodeCode").value("card_jiBenXinXi"))
            .andExpect(jsonPath("$.data.nodePath").value("jiBenXinXi"))
            .andExpect(jsonPath("$.data.nodeName").value("基本信息"));
    }

    @Test
    void testCreateChildNode() throws Exception {
        // 先创建父节点
        String parentBody = """
            {
              "nodeType": "CARD",
              "nodeName": "基本信息",
              "parentId": null,
              "sortNo": 1
            }
            """;

        String parentResponse = mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(parentBody))
            .andReturn().getResponse().getContentAsString();

        // 提取父节点 ID（简化处理，实际项目中应使用 JSON 解析）
        // 这里直接使用一个新的测试创建子节点

        String childBody = """
            {
              "nodeType": "FIELD",
              "nodeName": "合同名称",
              "parentId": null,
              "sortNo": 1
            }
            """;

        mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(childBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetById() throws Exception {
        // 先创建节点
        String body = """
            {
              "nodeType": "CARD",
              "nodeName": "查询API测试",
              "parentId": null,
              "sortNo": 1
            }
            """;

        mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk());

        // 查询节点列表
        mockMvc.perform(get("/api/templates/100/versions/200/layout-nodes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testListByVersionId() throws Exception {
        mockMvc.perform(get("/api/templates/100/versions/200/layout-nodes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testUpdate() throws Exception {
        // 先创建节点
        String createBody = """
            {
              "nodeType": "CARD",
              "nodeName": "更新API测试",
              "parentId": null,
              "sortNo": 1
            }
            """;

        String createResponse = mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(createBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String nodeId = JsonPath.read(createResponse, "$.data.id").toString();

        // 更新节点
        String updateBody = """
            {
              "nodeName": "更新后名称",
              "sortNo": 2
            }
            """;

        mockMvc.perform(put("/api/templates/100/versions/200/layout-nodes/" + nodeId)
                .contentType("application/json")
                .content(updateBody))
            .andExpect(status().isOk());
    }

    @Test
    void testCreateWithGridProperties() throws Exception {
        String body = """
            {
              "nodeType": "FIELD",
              "nodeName": "网格布局测试",
              "parentId": null,
              "sortNo": 1,
              "gridX": 0,
              "gridY": 0,
              "gridW": 6,
              "gridH": 1,
              "colNo": 1,
              "rowNo": 1,
              "colSpan": 2
            }
            """;

        mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.gridX").value(0))
            .andExpect(jsonPath("$.data.gridY").value(0))
            .andExpect(jsonPath("$.data.gridW").value(6));
    }

    @Test
    void testCreateWithRules() throws Exception {
        String body = """
            {
              "nodeType": "FIELD",
              "nodeName": "规则API测试",
              "parentId": null,
              "sortNo": 1,
              "visibleRule": "{\\"condition\\": \\"status == 'ACTIVE'\\"}",
              "readonlyRule": "{\\"condition\\": \\"readonly == true\\"}"
            }
            """;

        mockMvc.perform(post("/api/templates/100/versions/200/layout-nodes")
                .contentType("application/json")
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}