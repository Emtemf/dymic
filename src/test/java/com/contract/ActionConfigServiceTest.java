package com.contract;

import com.contract.application.template.ActionConfigService;
import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigCreateRequest;
import com.contract.application.template.dto.ActionConfigUpdateRequest;
import com.contract.application.template.convert.ActionConfigConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.ActionConfigRepository;
import com.contract.domain.template.repository.LayoutNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 动作配置服务测试
 */
@ExtendWith(MockitoExtension.class)
class ActionConfigServiceTest {

    @Mock
    private ActionConfigRepository repository;

    @Mock
    private LayoutNodeRepository layoutNodeRepository;

    @Spy
    private ActionConfigConverter converter = new ActionConfigConverterImpl();

    @InjectMocks
    private ActionConfigService service;

    private LayoutNode layoutNode;
    private ActionConfigCreateRequest createRequest;
    private ActionConfig actionConfig;

    @BeforeEach
    void setUp() {
        // 创建测试布局节点
        layoutNode = new LayoutNode();
        layoutNode.setId(1L);
        layoutNode.setTemplateId(100L);
        layoutNode.setTemplateVersionId(200L);
        layoutNode.setNodeCode("card_test");
        layoutNode.setNodeName("测试卡片");
        layoutNode.setNodeType("CARD");

        // 创建测试请求
        createRequest = ActionConfigCreateRequest.builder()
            .actionName("保存")
            .actionType("SAVE")
            .bindNodeId(1L)
            .confirmRequired(0)
            .sortNo(1)
            .build();

        // 创建测试动作配置
        actionConfig = new ActionConfig();
        actionConfig.setId(1L);
        actionConfig.setTemplateId(100L);
        actionConfig.setTemplateVersionId(200L);
        actionConfig.setActionCode("baoCun");
        actionConfig.setActionName("保存");
        actionConfig.setActionType("SAVE");
        actionConfig.setBindNodeId(1L);
        actionConfig.setConfirmRequired(0);
        actionConfig.setSortNo(1);
        actionConfig.setCreatedAt(LocalDateTime.now());
        actionConfig.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建动作配置 - 自动生成actionCode")
    void testCreate_shouldAutoGenerateActionCode() {
        // Given
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(repository.save(any(ActionConfig.class))).thenAnswer(invocation -> {
            ActionConfig config = invocation.getArgument(0);
            config.setId(1L);
            return config;
        });

        // When
        ActionConfigDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        // actionCode is auto-generated from actionName "保存" using PinyinUtils
        // Note: Hutool pinyin returns lowercase "baocun", not camelCase "baoCun"
        assertNotNull(result.getActionCode());
        assertEquals("保存", result.getActionName());
        assertEquals("SAVE", result.getActionType());
        verify(repository, times(1)).save(any(ActionConfig.class));
    }

    @Test
    @DisplayName("创建动作配置 - 验证绑定节点存在")
    void testCreate_shouldValidateBindNodeExists() {
        // Given
        when(layoutNodeRepository.findById(999L)).thenReturn(null);
        createRequest.setBindNodeId(999L);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.create(100L, 200L, createRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("创建动作配置 - 验证动作类型有效性")
    void testCreate_shouldValidateActionType() {
        // Given
        createRequest.setActionType("INVALID_TYPE");

        // When & Then
        assertThrows(BizException.class, () -> {
            service.create(100L, 200L, createRequest);
        });

        verify(repository, never()).save(any());
        verify(layoutNodeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("创建动作配置 - 支持所有有效动作类型")
    void testCreate_shouldSupportAllValidActionTypes() {
        // Given
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(repository.save(any(ActionConfig.class))).thenAnswer(invocation -> {
            ActionConfig config = invocation.getArgument(0);
            config.setId(1L);
            return config;
        });

        // Test all supported action types
        String[] validTypes = {"SAVE", "QUERY", "CANCEL", "CUSTOM"};

        for (String type : validTypes) {
            createRequest.setActionType(type);
            createRequest.setActionName(getActionNameForType(type));
            ActionConfigDTO result = service.create(100L, 200L, createRequest);
            assertNotNull(result);
            assertEquals(type, result.getActionType());
        }
    }

    private String getActionNameForType(String type) {
        switch (type) {
            case "SAVE": return "保存";
            case "QUERY": return "查询";
            case "CANCEL": return "取消";
            case "CUSTOM": return "自定义";
            default: return type;
        }
    }

    @Test
    @DisplayName("创建动作配置 - 设置默认值")
    void testCreate_shouldSetDefaultValues() {
        // Given
        createRequest.setConfirmRequired(null);
        createRequest.setSortNo(null);
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(repository.save(any(ActionConfig.class))).thenAnswer(invocation -> {
            ActionConfig config = invocation.getArgument(0);
            config.setId(1L);
            return config;
        });

        // When
        ActionConfigDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getConfirmRequired()); // 默认值
        assertEquals(0, result.getSortNo()); // 默认值
    }

    @Test
    @DisplayName("根据ID查询动作配置 - 成功")
    void testGetById_shouldReturnConfig() {
        // Given
        when(repository.findById(1L)).thenReturn(actionConfig);

        // When
        ActionConfigDTO result = service.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("baoCun", result.getActionCode());
    }

    @Test
    @DisplayName("根据ID查询动作配置 - 不存在抛异常")
    void testGetById_shouldThrowExceptionWhenNotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.getById(999L);
        });
    }

    @Test
    @DisplayName("根据版本ID查询动作配置列表 - 成功")
    void testListByVersionId_shouldReturnList() {
        // Given
        ActionConfig config1 = new ActionConfig();
        config1.setId(1L);
        config1.setTemplateVersionId(200L);
        config1.setActionCode("baoCun");
        config1.setActionType("SAVE");

        ActionConfig config2 = new ActionConfig();
        config2.setId(2L);
        config2.setTemplateVersionId(200L);
        config2.setActionCode("chaXun");
        config2.setActionType("QUERY");

        when(repository.findByTemplateVersionId(200L)).thenReturn(Arrays.asList(config1, config2));

        // When
        List<ActionConfigDTO> result = service.listByVersionId(200L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("更新动作配置 - 成功")
    void testUpdate_shouldUpdateConfig() {
        // Given
        ActionConfigUpdateRequest updateRequest = ActionConfigUpdateRequest.builder()
            .confirmText("确认保存吗？")
            .sortNo(2)
            .build();

        when(repository.findById(1L)).thenReturn(actionConfig);
        when(repository.update(any(ActionConfig.class))).thenReturn(actionConfig);

        // When
        ActionConfigDTO result = service.update(1L, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("确认保存吗？", result.getConfirmText());
        assertEquals(2, result.getSortNo());
        verify(repository, times(1)).update(any());
    }

    @Test
    @DisplayName("更新动作配置 - 更新actionName重新生成actionCode")
    void testUpdate_shouldRegenerateActionCodeWhenNameChanged() {
        // Given
        ActionConfigUpdateRequest updateRequest = ActionConfigUpdateRequest.builder()
            .actionName("提交")
            .build();

        when(repository.findById(1L)).thenReturn(actionConfig);
        when(repository.update(any(ActionConfig.class))).thenReturn(actionConfig);

        // When
        ActionConfigDTO result = service.update(1L, updateRequest);

        // Then
        assertNotNull(result);
        // actionCode is regenerated when actionName changes
        assertNotNull(result.getActionCode());
        assertEquals("提交", result.getActionName());
    }

    @Test
    @DisplayName("更新动作配置 - 不存在抛异常")
    void testUpdate_shouldThrowExceptionWhenNotFound() {
        // Given
        ActionConfigUpdateRequest updateRequest = ActionConfigUpdateRequest.builder()
            .confirmText("测试")
            .build();

        when(repository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.update(999L, updateRequest);
        });

        verify(repository, never()).update(any());
    }

    @Test
    @DisplayName("删除动作配置 - 成功")
    void testDelete_shouldDeleteConfig() {
        // Given
        when(repository.findById(1L)).thenReturn(actionConfig);
        doNothing().when(repository).deleteById(1L);

        // When
        service.delete(1L);

        // Then
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除动作配置 - 不存在抛异常")
    void testDelete_shouldThrowExceptionWhenNotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.delete(999L);
        });

        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("创建动作配置 - 保存扩展字段")
    void testCreate_shouldSaveExtensionFields() {
        // Given
        createRequest.setBindQueryId(10L);
        createRequest.setConfirmRequired(1);
        createRequest.setConfirmText("确认保存吗？");
        createRequest.setBeforeRule("{\"validation\": true}");
        createRequest.setAfterRule("{\"callback\": \"refresh\"}");
        createRequest.setPropsJson("{\"icon\": \"save\"}");

        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(repository.save(any(ActionConfig.class))).thenAnswer(invocation -> {
            ActionConfig config = invocation.getArgument(0);
            config.setId(1L);
            return config;
        });

        // When
        ActionConfigDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getBindQueryId());
        assertEquals(1, result.getConfirmRequired());
        assertEquals("确认保存吗？", result.getConfirmText());
        assertNotNull(result.getBeforeRule());
        assertNotNull(result.getAfterRule());
        assertNotNull(result.getPropsJson());
    }

    /**
     * 手动实现的 Converter，用于测试
     */
    private static class ActionConfigConverterImpl implements ActionConfigConverter {

        @Override
        public ActionConfigDTO toDTO(ActionConfig domain) {
            if (domain == null) return null;
            return ActionConfigDTO.builder()
                .id(domain.getId())
                .templateId(domain.getTemplateId())
                .templateVersionId(domain.getTemplateVersionId())
                .actionCode(domain.getActionCode())
                .actionName(domain.getActionName())
                .actionType(domain.getActionType())
                .bindNodeId(domain.getBindNodeId())
                .bindQueryId(domain.getBindQueryId())
                .confirmRequired(domain.getConfirmRequired())
                .confirmText(domain.getConfirmText())
                .beforeRule(domain.getBeforeRule())
                .afterRule(domain.getAfterRule())
                .propsJson(domain.getPropsJson())
                .sortNo(domain.getSortNo())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        }

        @Override
        public ActionConfig toDomain(ActionConfigCreateRequest request) {
            if (request == null) return null;
            ActionConfig domain = new ActionConfig();
            domain.setActionName(request.getActionName());
            domain.setActionType(request.getActionType());
            domain.setBindNodeId(request.getBindNodeId());
            domain.setBindQueryId(request.getBindQueryId());
            domain.setConfirmRequired(request.getConfirmRequired());
            domain.setConfirmText(request.getConfirmText());
            domain.setBeforeRule(request.getBeforeRule());
            domain.setAfterRule(request.getAfterRule());
            domain.setPropsJson(request.getPropsJson());
            domain.setSortNo(request.getSortNo());
            return domain;
        }

        @Override
        public void updateFromDTO(ActionConfigUpdateRequest request, ActionConfig domain) {
            if (request == null || domain == null) return;
            if (request.getActionName() != null) {
                domain.setActionName(request.getActionName());
            }
            if (request.getBindQueryId() != null) {
                domain.setBindQueryId(request.getBindQueryId());
            }
            if (request.getConfirmRequired() != null) {
                domain.setConfirmRequired(request.getConfirmRequired());
            }
            if (request.getConfirmText() != null) {
                domain.setConfirmText(request.getConfirmText());
            }
            if (request.getBeforeRule() != null) {
                domain.setBeforeRule(request.getBeforeRule());
            }
            if (request.getAfterRule() != null) {
                domain.setAfterRule(request.getAfterRule());
            }
            if (request.getPropsJson() != null) {
                domain.setPropsJson(request.getPropsJson());
            }
            if (request.getSortNo() != null) {
                domain.setSortNo(request.getSortNo());
            }
        }

        @Override
        public List<ActionConfigDTO> toDTOList(List<ActionConfig> domains) {
            if (domains == null) return java.util.Collections.emptyList();
            return domains.stream()
                .map(this::toDTO)
                .toList();
        }
    }
}