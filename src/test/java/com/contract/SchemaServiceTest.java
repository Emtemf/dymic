package com.contract;

import com.contract.application.template.SchemaService;
import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.FieldDefService;
import com.contract.application.template.FieldComponentService;
import com.contract.application.template.ActionConfigService;
import com.contract.application.template.TemplateVersionService;
import com.contract.application.template.TemplateService;
import com.contract.application.template.dto.*;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Schema聚合服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class SchemaServiceTest {

    @Autowired
    private SchemaService schemaService;

    @MockBean
    private LayoutNodeService layoutNodeService;

    @MockBean
    private FieldDefService fieldDefService;

    @MockBean
    private FieldComponentService fieldComponentService;

    @MockBean
    private ActionConfigService actionConfigService;

    @MockBean
    private TemplateVersionService templateVersionService;

    @MockBean
    private TemplateService templateService;

    private Template template;
    private TemplateVersion version;
    private LayoutNodeDTO parentNode;
    private LayoutNodeDTO childNode;
    private FieldComponentDTO component;
    private ActionConfigDTO action;

    @BeforeEach
    void setUp() {
        // Setup template
        template = new Template();
        template.setId(1L);
        template.setTemplateCode("TPL001");
        template.setTemplateName("测试模板");
        template.setTemplateDesc("测试模板描述");
        template.setBizType("SALE");

        // Setup version
        version = new TemplateVersion();
        version.setId(1L);
        version.setTemplateId(1L);
        version.setVersionNo(1);
        version.setVersionName("V1.0");
        version.setVersionStatus("PUBLISHED");

        // Setup parent layout node
        parentNode = LayoutNodeDTO.builder()
            .id(1L)
            .templateId(1L)
            .templateVersionId(1L)
            .parentId(null)
            .nodeCode("card_basicInfo")
            .nodePath("basicInfo")
            .nodeName("基本信息")
            .nodeType("CARD")
            .sortNo(1)
            .gridX(0)
            .gridY(0)
            .gridW(12)
            .gridH(1)
            .build();

        // Setup child layout node
        childNode = LayoutNodeDTO.builder()
            .id(2L)
            .templateId(1L)
            .templateVersionId(1L)
            .parentId(1L)
            .nodeCode("field_contractName")
            .nodePath("basicInfo.contractName")
            .nodeName("合同名称")
            .nodeType("FIELD")
            .sortNo(1)
            .gridX(0)
            .gridY(0)
            .gridW(6)
            .gridH(1)
            .build();

        // Setup field component
        component = FieldComponentDTO.builder()
            .id(1L)
            .templateId(1L)
            .templateVersionId(1L)
            .fieldDefId(1L)
            .layoutNodeId(2L)
            .componentType("INPUT")
            .labelName("合同名称")
            .placeholder("请输入合同名称")
            .sortNo(1)
            .build();

        // Setup action config
        action = ActionConfigDTO.builder()
            .id(1L)
            .templateId(1L)
            .templateVersionId(1L)
            .actionCode("baoCun")
            .actionName("保存")
            .actionType("SAVE")
            .bindNodeId(1L)
            .sortNo(1)
            .build();
    }

    @Test
    void testGetSchema_shouldReturnCompleteConfigTree() {
        // Given
        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(parentNode, childNode));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Arrays.asList(component));
        when(actionConfigService.listByVersionId(1L)).thenReturn(Arrays.asList(action));

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then
        assertNotNull(schema);
        assertEquals(1L, schema.getTemplateId());
        assertEquals(1L, schema.getTemplateVersionId());
        assertEquals("TPL001", schema.getTemplateCode());
        assertEquals("测试模板", schema.getTemplateName());
        assertEquals("SALE", schema.getBizType());
        assertEquals(1, schema.getVersionNo());
        assertEquals("V1.0", schema.getVersionName());
        assertEquals("PUBLISHED", schema.getVersionStatus());

        // Verify tree structure
        assertNotNull(schema.getLayoutNodes());
        assertTrue(schema.getLayoutNodes().size() > 0);

        // Verify lists
        assertNotNull(schema.getFieldDefs());
        assertNotNull(schema.getFieldComponents());
        assertNotNull(schema.getActionConfigs());
    }

    @Test
    void testGetSchema_shouldBuildNestedTreeStructure() {
        // Given
        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(parentNode, childNode));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(actionConfigService.listByVersionId(1L)).thenReturn(Collections.emptyList());

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify tree structure
        List<LayoutNodeTreeDTO> rootNodes = schema.getLayoutNodes();
        assertEquals(1, rootNodes.size());  // Only one root node

        LayoutNodeTreeDTO root = rootNodes.get(0);
        assertEquals(1L, root.getId());
        assertEquals("card_basicInfo", root.getNodeCode());
        assertEquals("basicInfo", root.getNodePath());
        assertEquals("基本信息", root.getNodeName());
        assertEquals("CARD", root.getNodeType());

        // Verify child nodes
        assertNotNull(root.getChildren());
        assertEquals(1, root.getChildren().size());
        LayoutNodeTreeDTO child = root.getChildren().get(0);
        assertEquals(2L, child.getId());
        assertEquals("field_contractName", child.getNodeCode());
        assertEquals("basicInfo.contractName", child.getNodePath());
    }

    @Test
    void testGetSchema_shouldBindComponentsToNode() {
        // Given
        FieldComponentDTO component1 = FieldComponentDTO.builder()
            .id(1L)
            .layoutNodeId(2L)
            .componentType("INPUT")
            .labelName("合同名称")
            .sortNo(1)
            .build();

        FieldComponentDTO component2 = FieldComponentDTO.builder()
            .id(2L)
            .layoutNodeId(2L)
            .componentType("INPUT")
            .labelName("合同编号")
            .sortNo(2)
            .build();

        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(parentNode, childNode));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Arrays.asList(component1, component2));
        when(actionConfigService.listByVersionId(1L)).thenReturn(Collections.emptyList());

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify components bind to correct node
        LayoutNodeTreeDTO root = schema.getLayoutNodes().get(0);
        LayoutNodeTreeDTO child = root.getChildren().get(0);

        assertNotNull(child.getComponents());
        assertEquals(2, child.getComponents().size());
        assertEquals("合同名称", child.getComponents().get(0).getLabelName());
        assertEquals("合同编号", child.getComponents().get(1).getLabelName());
    }

    @Test
    void testGetSchema_shouldBindActionsToNode() {
        // Given
        ActionConfigDTO action1 = ActionConfigDTO.builder()
            .id(1L)
            .bindNodeId(1L)
            .actionCode("baoCun")
            .actionName("保存")
            .actionType("SAVE")
            .sortNo(1)
            .build();

        ActionConfigDTO action2 = ActionConfigDTO.builder()
            .id(2L)
            .bindNodeId(1L)
            .actionCode("quXiao")
            .actionName("取消")
            .actionType("CANCEL")
            .sortNo(2)
            .build();

        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(parentNode, childNode));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(actionConfigService.listByVersionId(1L)).thenReturn(Arrays.asList(action1, action2));

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify actions bind to correct node
        LayoutNodeTreeDTO root = schema.getLayoutNodes().get(0);

        assertNotNull(root.getActions());
        assertEquals(2, root.getActions().size());
        assertEquals("保存", root.getActions().get(0).getActionName());
        assertEquals("取消", root.getActions().get(1).getActionName());
    }

    @Test
    void testGetSchema_shouldSortNodesCorrectly() {
        // Given: Create multiple nodes with different sortNo
        LayoutNodeDTO node1 = LayoutNodeDTO.builder()
            .id(1L)
            .parentId(null)
            .nodeCode("card_node1")
            .nodePath("node1")
            .nodeName("节点1")
            .nodeType("CARD")
            .sortNo(3)
            .build();

        LayoutNodeDTO node2 = LayoutNodeDTO.builder()
            .id(2L)
            .parentId(null)
            .nodeCode("card_node2")
            .nodePath("node2")
            .nodeName("节点2")
            .nodeType("CARD")
            .sortNo(1)
            .build();

        LayoutNodeDTO node3 = LayoutNodeDTO.builder()
            .id(3L)
            .parentId(null)
            .nodeCode("card_node3")
            .nodePath("node3")
            .nodeName("节点3")
            .nodeType("CARD")
            .sortNo(2)
            .build();

        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(node1, node2, node3));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(actionConfigService.listByVersionId(1L)).thenReturn(Collections.emptyList());

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify nodes are sorted by sortNo
        List<LayoutNodeTreeDTO> nodes = schema.getLayoutNodes();
        assertEquals(3, nodes.size());
        assertEquals("节点2", nodes.get(0).getNodeName());  // sortNo=1
        assertEquals("节点3", nodes.get(1).getNodeName());  // sortNo=2
        assertEquals("节点1", nodes.get(2).getNodeName());  // sortNo=3
    }

    @Test
    void testGetSchema_shouldIncludeGridProperties() {
        // Given
        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(parentNode, childNode));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(actionConfigService.listByVersionId(1L)).thenReturn(Collections.emptyList());

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify grid properties included
        LayoutNodeTreeDTO root = schema.getLayoutNodes().get(0);
        assertEquals(0, root.getGridX());
        assertEquals(0, root.getGridY());
        assertEquals(12, root.getGridW());
        assertEquals(1, root.getGridH());
    }

    @Test
    void testGetSchema_shouldIncludeRules() {
        // Given
        LayoutNodeDTO nodeWithRules = LayoutNodeDTO.builder()
            .id(1L)
            .parentId(null)
            .nodeCode("card_test")
            .nodePath("test")
            .nodeName("测试节点")
            .nodeType("CARD")
            .sortNo(1)
            .visibleRule("{\"condition\": \"status == 'ACTIVE'\"}")
            .readonlyRule("{\"condition\": \"readonly == true\"}")
            .build();

        when(templateService.getById(1L)).thenReturn(template);
        when(templateVersionService.getById(1L)).thenReturn(version);
        when(layoutNodeService.listByVersionId(1L)).thenReturn(Arrays.asList(nodeWithRules));
        when(fieldDefService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(fieldComponentService.listByVersionId(1L)).thenReturn(Collections.emptyList());
        when(actionConfigService.listByVersionId(1L)).thenReturn(Collections.emptyList());

        // When
        SchemaDTO schema = schemaService.getSchema(1L, 1L);

        // Then: Verify rules included
        LayoutNodeTreeDTO node = schema.getLayoutNodes().get(0);
        assertNotNull(node.getVisibleRule());
        assertNotNull(node.getReadonlyRule());
    }
}