package com.contract;

import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 布局节点服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class LayoutNodeServiceTest {
    @Autowired
    private LayoutNodeService service;

    @Test
    void testCreateLayoutNode() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName("基本信息");
        dto.setParentId(null);
        dto.setSortNo(1);
        dto.setLevelNo(1);

        // 使用唯一的 versionId 避免冲突
        LayoutNodeDTO result = service.create(100L, 900L, dto);

        assertNotNull(result.getId());
        assertEquals("card_jiBenXinXi", result.getNodeCode());
        assertEquals("jiBenXinXi", result.getNodePath());
        assertEquals("基本信息", result.getNodeName());
        assertEquals("CARD", result.getNodeType());
    }

    @Test
    void testCreateChildNode() {
        // 先创建父节点
        LayoutNodeCreateDTO parentDto = new LayoutNodeCreateDTO();
        parentDto.setNodeType("CARD");
        parentDto.setDisplayName("基本信息");
        parentDto.setParentId(null);
        parentDto.setSortNo(1);

        LayoutNodeDTO parent = service.create(100L, 201L, parentDto);

        // 创建子节点
        LayoutNodeCreateDTO childDto = new LayoutNodeCreateDTO();
        childDto.setNodeType("FIELD");
        childDto.setDisplayName("合同名称");
        childDto.setParentId(parent.getId());
        childDto.setSortNo(1);

        LayoutNodeDTO child = service.create(100L, 201L, childDto);

        assertNotNull(child.getId());
        assertEquals("field_heTongMingCheng", child.getNodeCode());
        assertTrue(child.getNodePath().startsWith(parent.getNodePath()));
        assertTrue(child.getNodePath().endsWith(".heTongMingCheng"));
    }

    @Test
    void testGetById() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName("查询测试");
        dto.setParentId(null);
        dto.setSortNo(1);

        LayoutNodeDTO created = service.create(100L, 202L, dto);
        LayoutNodeDTO result = service.getById(created.getId());

        assertEquals("查询测试", result.getNodeName());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(BizException.class, () -> service.getById(999999L));
    }

    @Test
    void testUpdate() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName("更新测试");
        dto.setParentId(null);
        dto.setSortNo(1);

        LayoutNodeDTO created = service.create(100L, 203L, dto);

        LayoutNodeUpdateDTO updateDto = new LayoutNodeUpdateDTO();
        updateDto.setNodeName("更新后名称");
        updateDto.setSortNo(2);

        LayoutNodeDTO result = service.update(created.getId(), updateDto);
        assertEquals("更新后名称", result.getNodeName());
        assertEquals(2, result.getSortNo());
    }

    @Test
    void testListByVersionId() {
        LayoutNodeCreateDTO dto1 = new LayoutNodeCreateDTO();
        dto1.setNodeType("CARD");
        dto1.setDisplayName("列表测试1");
        dto1.setParentId(null);
        dto1.setSortNo(1);
        service.create(100L, 304L, dto1);

        LayoutNodeCreateDTO dto2 = new LayoutNodeCreateDTO();
        dto2.setNodeType("CARD");
        dto2.setDisplayName("列表测试2");
        dto2.setParentId(null);
        dto2.setSortNo(2);
        service.create(100L, 304L, dto2);

        List<LayoutNodeDTO> result = service.listByVersionId(304L);
        assertTrue(result.size() >= 2);
    }

    @Test
    void testCreateWithGridProperties() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("FIELD");
        dto.setDisplayName("网格布局");
        dto.setParentId(null);
        dto.setSortNo(1);
        dto.setGridX(0);
        dto.setGridY(0);
        dto.setGridW(6);
        dto.setGridH(1);
        dto.setColNo(1);
        dto.setRowNo(1);
        dto.setColSpan(2);

        LayoutNodeDTO result = service.create(100L, 205L, dto);

        assertNotNull(result.getId());
        assertEquals(0, result.getGridX());
        assertEquals(0, result.getGridY());
        assertEquals(6, result.getGridW());
        assertEquals(1, result.getGridH());
    }

    @Test
    void testCreateWithRules() {
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("FIELD");
        dto.setDisplayName("规则测试");
        dto.setParentId(null);
        dto.setSortNo(1);

        // 设置可见规则
        dto.setVisibleRule("{\"condition\": \"status == 'ACTIVE'\"}");

        // 设置只读规则
        dto.setReadonlyRule("{\"condition\": \"readonly == true\"}");

        LayoutNodeDTO result = service.create(100L, 206L, dto);

        assertNotNull(result.getId());
        assertNotNull(result.getVisibleRule());
        assertNotNull(result.getReadonlyRule());
    }

    @Test
    void testDeleteById() {
        // 创建节点
        LayoutNodeCreateDTO dto = new LayoutNodeCreateDTO();
        dto.setNodeType("CARD");
        dto.setDisplayName("删除测试");
        dto.setParentId(null);
        dto.setSortNo(1);

        LayoutNodeDTO created = service.create(100L, 207L, dto);
        assertNotNull(created.getId());

        // 删除节点
        service.deleteById(created.getId());

        // 验证删除后查询会抛出异常
        assertThrows(BizException.class, () -> service.getById(created.getId()));
    }

    @Test
    void testDeleteByIdNotFound() {
        assertThrows(BizException.class, () -> service.deleteById(999999L));
    }
}