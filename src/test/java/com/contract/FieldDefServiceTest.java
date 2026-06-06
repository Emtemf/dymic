package com.contract;

import com.contract.application.template.FieldDefService;
import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.dto.FieldDefDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefCreateResult;
import com.contract.application.template.dto.FieldDefUpdateDTO;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 字段定义服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class FieldDefServiceTest {
    @Autowired
    private FieldDefService service;

    @Autowired
    private LayoutNodeService layoutNodeService;

    /**
     * 创建测试用的布局节点
     */
    private LayoutNodeDTO createTestLayoutNode(Long templateId, Long versionId, String displayName) {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName(displayName);
        dto.setParentId(null);
        dto.setSortNo(1);
        dto.setLevelNo(1);
        return layoutNodeService.create(templateId, versionId, dto);
    }

    @Test
    void testCreateFieldDef() {
        // 先创建布局节点
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 900L, "基本信息");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("合同名称");
        dto.setComponentType("INPUT");
        dto.setRequired(false);
        dto.setPlaceholder("请输入合同名称");
        dto.setSortNo(1);

        FieldDefCreateResult result = service.create(100L, 900L, dto);

        assertNotNull(result.getFieldDef());
        assertNotNull(result.getFieldDef().getId());
        assertEquals("hetongmingcheng", result.getFieldDef().getFieldCode());
        assertTrue(result.getFieldDef().getFieldPath().contains("hetongmingcheng"));
        assertEquals("合同名称", result.getFieldDef().getFieldNameCn());
        assertEquals("TEXT", result.getFieldDef().getDataType());
        assertEquals(0, result.getFieldDef().getRequiredDefault());

        // 验证字段组件绑定同时创建
        assertNotNull(result.getFieldComponent());
        assertNotNull(result.getFieldComponent().getId());
        assertEquals("INPUT", result.getFieldComponent().getComponentType());
    }

    @Test
    void testCreateNumberField() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 901L, "金额信息");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("合同金额");
        dto.setComponentType("NUMBER");
        dto.setRequired(true);
        dto.setSortNo(1);

        FieldDefCreateResult result = service.create(100L, 901L, dto);

        assertEquals("NUMBER", result.getFieldDef().getDataType());
        assertEquals(1, result.getFieldDef().getRequiredDefault());
    }

    @Test
    void testCreateDateField() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 902L, "时间信息");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("签订日期");
        dto.setComponentType("DATE");
        dto.setRequired(false);
        dto.setSortNo(1);

        FieldDefCreateResult result = service.create(100L, 902L, dto);

        assertEquals("DATE", result.getFieldDef().getDataType());
    }

    @Test
    void testCreateMoneyField() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 903L, "财务信息");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("付款金额");
        dto.setComponentType("MONEY");
        dto.setRequired(false);
        dto.setSortNo(1);

        FieldDefCreateResult result = service.create(100L, 903L, dto);

        assertEquals("NUMBER", result.getFieldDef().getDataType());
    }

    @Test
    void testCreateSelectFieldWithStaticOptions() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 904L, "选择信息");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("合同类型");
        dto.setComponentType("SELECT");
        dto.setRequired(false);
        dto.setDataSourceType("STATIC");

        List<FieldDefCreateDTO.StaticOption> options = new ArrayList<>();
        options.add(new FieldDefCreateDTO.StaticOption("PURCHASE", "采购合同", 1));
        options.add(new FieldDefCreateDTO.StaticOption("SALES", "销售合同", 2));
        dto.setStaticOptions(options);
        dto.setSortNo(1);

        FieldDefCreateResult result = service.create(100L, 904L, dto);

        assertEquals("TEXT", result.getFieldDef().getDataType());
        assertNotNull(result.getFieldComponent().getComponentProps());
    }

    @Test
    void testCreateFieldWithNonExistentLayoutNode() {
        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(999999L);
        dto.setDisplayName("测试字段");
        dto.setComponentType("INPUT");
        dto.setRequired(false);

        assertThrows(BizException.class, () -> service.create(100L, 905L, dto));
    }

    @Test
    void testGetById() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 906L, "查询测试");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("查询字段");
        dto.setComponentType("INPUT");
        dto.setRequired(false);

        FieldDefCreateResult created = service.create(100L, 906L, dto);
        FieldDefDTO result = service.getById(created.getFieldDef().getId());

        assertEquals("查询字段", result.getFieldNameCn());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(BizException.class, () -> service.getById(999999L));
    }

    @Test
    void testListByVersionId() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 907L, "列表测试");

        FieldDefCreateDTO dto1 = new FieldDefCreateDTO();
        dto1.setLayoutNodeId(layoutNode.getId());
        dto1.setDisplayName("列表字段1");
        dto1.setComponentType("INPUT");
        dto1.setRequired(false);
        service.create(100L, 907L, dto1);

        FieldDefCreateDTO dto2 = new FieldDefCreateDTO();
        dto2.setLayoutNodeId(layoutNode.getId());
        dto2.setDisplayName("列表字段2");
        dto2.setComponentType("INPUT");
        dto2.setRequired(false);
        service.create(100L, 907L, dto2);

        List<FieldDefDTO> result = service.listByVersionId(907L);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testUpdate() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 908L, "更新测试");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("更新字段");
        dto.setComponentType("INPUT");
        dto.setRequired(false);

        FieldDefCreateResult created = service.create(100L, 908L, dto);

        FieldDefUpdateDTO updateDto = new FieldDefUpdateDTO();
        updateDto.setFieldNameCn("更新后名称");
        updateDto.setRequiredDefault(1);

        FieldDefDTO result = service.update(created.getFieldDef().getId(), updateDto);
        assertEquals("更新后名称", result.getFieldNameCn());
        assertEquals(1, result.getRequiredDefault());
    }

    @Test
    void testUpdateNotFound() {
        FieldDefUpdateDTO updateDto = new FieldDefUpdateDTO();
        updateDto.setFieldNameCn("更新后名称");

        assertThrows(BizException.class, () -> service.update(999999L, updateDto));
    }

    @Test
    void testAutoGenerateFieldCode() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 909L, "编码测试");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("合同编号");
        dto.setComponentType("INPUT");
        dto.setRequired(false);

        FieldDefCreateResult result = service.create(100L, 909L, dto);

        // 验证拼音驼峰生成：合同编号 → hetongbianhao
        assertEquals("hetongbianhao", result.getFieldDef().getFieldCode());
    }

    @Test
    void testAutoGenerateFieldPath() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 910L, "路径测试");

        FieldDefCreateDTO dto = new FieldDefCreateDTO();
        dto.setLayoutNodeId(layoutNode.getId());
        dto.setDisplayName("供应商名称");
        dto.setComponentType("INPUT");
        dto.setRequired(false);

        FieldDefCreateResult result = service.create(100L, 910L, dto);

        // 验证字段路径生成：节点路径.字段编码
        assertNotNull(result.getFieldDef().getFieldPath());
        assertTrue(result.getFieldDef().getFieldPath().contains("gongyingshangmingcheng"));
        assertTrue(result.getFieldDef().getFieldPath().contains(layoutNode.getNodePath()));
    }

    @Test
    void testCreateMultipleFieldsOnSameLayoutNode() {
        LayoutNodeDTO layoutNode = createTestLayoutNode(100L, 911L, "多字段测试");

        FieldDefCreateDTO dto1 = new FieldDefCreateDTO();
        dto1.setLayoutNodeId(layoutNode.getId());
        dto1.setDisplayName("字段A");
        dto1.setComponentType("INPUT");
        dto1.setRequired(false);
        dto1.setSortNo(1);

        FieldDefCreateDTO dto2 = new FieldDefCreateDTO();
        dto2.setLayoutNodeId(layoutNode.getId());
        dto2.setDisplayName("字段B");
        dto2.setComponentType("INPUT");
        dto2.setRequired(false);
        dto2.setSortNo(2);

        FieldDefCreateDTO dto3 = new FieldDefCreateDTO();
        dto3.setLayoutNodeId(layoutNode.getId());
        dto3.setDisplayName("字段C");
        dto3.setComponentType("INPUT");
        dto3.setRequired(false);
        dto3.setSortNo(3);

        FieldDefCreateResult result1 = service.create(100L, 911L, dto1);
        FieldDefCreateResult result2 = service.create(100L, 911L, dto2);
        FieldDefCreateResult result3 = service.create(100L, 911L, dto3);

        assertNotNull(result1.getFieldDef().getId());
        assertNotNull(result2.getFieldDef().getId());
        assertNotNull(result3.getFieldDef().getId());

        List<FieldDefDTO> fields = service.listByVersionId(911L);
        assertTrue(fields.size() >= 3);
    }
}