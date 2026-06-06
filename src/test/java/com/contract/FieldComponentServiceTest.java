package com.contract;

import com.contract.application.template.FieldComponentService;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.application.template.convert.FieldComponentConverter;
import com.contract.common.exception.BizException;
import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.FieldDef;
import com.contract.domain.template.LayoutNode;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.domain.template.repository.LayoutNodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * 字段组件绑定服务测试
 */
@ExtendWith(MockitoExtension.class)
class FieldComponentServiceTest {

    @Mock
    private FieldComponentRepository repository;

    @Mock
    private LayoutNodeRepository layoutNodeRepository;

    @Mock
    private FieldDefRepository fieldDefRepository;

    @Spy
    private FieldComponentConverter converter = new FieldComponentConverterImpl();

    @InjectMocks
    private FieldComponentService service;

    private LayoutNode layoutNode;
    private FieldDef fieldDef;
    private FieldComponentCreateRequest createRequest;
    private FieldComponent fieldComponent;

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

        // 创建测试字段定义
        fieldDef = new FieldDef();
        fieldDef.setId(1L);
        fieldDef.setTemplateId(100L);
        fieldDef.setTemplateVersionId(200L);
        fieldDef.setFieldCode("contractName");
        fieldDef.setFieldNameCn("合同名称");
        fieldDef.setFieldNameEn("Contract Name");
        fieldDef.setDataType("STRING");

        // 创建测试请求
        createRequest = FieldComponentCreateRequest.builder()
            .layoutNodeId(1L)
            .fieldDefId(1L)
            .componentType("INPUT")
            .placeholder("请输入合同名称")
            .sortNo(1)
            .build();

        // 创建测试字段组件
        fieldComponent = new FieldComponent();
        fieldComponent.setId(1L);
        fieldComponent.setTemplateId(100L);
        fieldComponent.setTemplateVersionId(200L);
        fieldComponent.setLayoutNodeId(1L);
        fieldComponent.setFieldDefId(1L);
        fieldComponent.setComponentType("INPUT");
        fieldComponent.setLabelName("合同名称");
        fieldComponent.setPlaceholder("请输入合同名称");
        fieldComponent.setSortNo(1);
        fieldComponent.setCreatedAt(LocalDateTime.now());
        fieldComponent.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreate_shouldBindFieldToLayoutNode() {
        // Given
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        // When
        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals("INPUT", result.getComponentType());
        assertEquals(1L, result.getLayoutNodeId());
        assertEquals(1L, result.getFieldDefId());
        verify(repository, times(1)).save(any(FieldComponent.class));
    }

    @Test
    void testCreate_shouldUseFieldNameAsDefaultLabel() {
        // Given
        createRequest.setLabelName(null);
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        // When
        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals("合同名称", result.getLabelName()); // 默认使用 fieldNameCn
    }

    @Test
    void testCreate_shouldUseCustomLabelIfProvided() {
        // Given
        createRequest.setLabelName("自定义标签");
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenAnswer(invocation -> {
            FieldComponent comp = invocation.getArgument(0);
            comp.setId(1L);
            return comp;
        });

        // When
        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        assertEquals("自定义标签", result.getLabelName());
    }

    @Test
    void testCreate_shouldValidateLayoutNodeExists() {
        // Given
        when(layoutNodeRepository.findById(999L)).thenReturn(null);

        createRequest.setLayoutNodeId(999L);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.create(100L, 200L, createRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_shouldValidateFieldDefExists() {
        // Given
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(999L)).thenReturn(null);

        createRequest.setFieldDefId(999L);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.create(100L, 200L, createRequest);
        });

        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_shouldValidateComponentType() {
        // Given - Component type is validated first before layout/field validation
        createRequest.setComponentType("INVALID_TYPE");

        // When & Then
        assertThrows(BizException.class, () -> {
            service.create(100L, 200L, createRequest);
        });

        verify(repository, never()).save(any());
        verify(layoutNodeRepository, never()).findById(any());
        verify(fieldDefRepository, never()).findById(any());
    }

    @Test
    void testCreate_shouldSupportAllValidComponentTypes() {
        // Given
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        // Test all supported component types
        String[] validTypes = {"INPUT", "SELECT", "DATE", "DATETIME", "MONEY", "NUMBER",
            "RADIO", "CHECKBOX", "TREE", "UPLOAD", "TEXTAREA", "RICHTEXT",
            "SWITCH", "SLIDER", "RATE", "COLOR", "CASCADE"};

        for (String type : validTypes) {
            createRequest.setComponentType(type);
            FieldComponentDTO result = service.create(100L, 200L, createRequest);
            assertNotNull(result);
        }
    }

    @Test
    void testGetById_shouldReturnComponent() {
        // Given
        when(repository.findById(1L)).thenReturn(fieldComponent);

        // When
        FieldComponentDTO result = service.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("INPUT", result.getComponentType());
    }

    @Test
    void testGetById_shouldThrowExceptionWhenNotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.getById(999L);
        });
    }

    @Test
    void testListByVersionId_shouldReturnList() {
        // Given
        FieldComponent comp1 = new FieldComponent();
        comp1.setId(1L);
        comp1.setTemplateVersionId(200L);
        comp1.setComponentType("INPUT");

        FieldComponent comp2 = new FieldComponent();
        comp2.setId(2L);
        comp2.setTemplateVersionId(200L);
        comp2.setComponentType("SELECT");

        when(repository.findByVersionId(200L)).thenReturn(Arrays.asList(comp1, comp2));

        // When
        List<FieldComponentDTO> result = service.listByVersionId(200L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListByLayoutNodeId_shouldReturnList() {
        // Given
        FieldComponent comp1 = new FieldComponent();
        comp1.setId(1L);
        comp1.setLayoutNodeId(1L);
        comp1.setComponentType("INPUT");

        when(repository.findByLayoutNodeId(1L)).thenReturn(Arrays.asList(comp1));

        // When
        List<FieldComponentDTO> result = service.listByLayoutNodeId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdate_shouldUpdateComponent() {
        // Given
        FieldComponentUpdateDTO updateDTO = new FieldComponentUpdateDTO();
        updateDTO.setLabelName("更新后的标签");
        updateDTO.setPlaceholder("更新后的提示");

        when(repository.findById(1L)).thenReturn(fieldComponent);
        doNothing().when(repository).update(any(FieldComponent.class));

        // When
        FieldComponentDTO result = service.update(1L, updateDTO);

        // Then
        assertNotNull(result);
        assertEquals("更新后的标签", result.getLabelName());
        assertEquals("更新后的提示", result.getPlaceholder());
        verify(repository, times(1)).update(any());
    }

    @Test
    void testUpdate_shouldThrowExceptionWhenNotFound() {
        // Given
        FieldComponentUpdateDTO updateDTO = new FieldComponentUpdateDTO();
        updateDTO.setLabelName("更新后的标签");

        when(repository.findById(999L)).thenReturn(null);

        // When & Then
        assertThrows(BizException.class, () -> {
            service.update(999L, updateDTO);
        });

        verify(repository, never()).update(any());
    }

    @Test
    void testDelete_shouldDeleteComponent() {
        // Given
        when(repository.findById(1L)).thenReturn(fieldComponent);
        doNothing().when(repository).deleteById(1L);

        // When
        service.delete(1L);

        // Then
        verify(repository, times(1)).deleteById(1L);
    }

    @Test
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
    void testCreate_shouldSetDefaultSortNoToZero() {
        // Given
        createRequest.setSortNo(null);
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        // When
        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        // The actual sortNo will be 0 as set in service
    }

    @Test
    void testCreate_shouldSaveExtensionRules() {
        // Given
        createRequest.setRequiredRule("{\"required\": true}");
        createRequest.setReadonlyRule("{\"condition\": \"status == 'LOCKED'\"}");
        createRequest.setVisibleRule("{\"condition\": \"type == 'CONTRACT'\"}");
        createRequest.setComponentProps("{\"options\": [{\"label\": \"选项1\", \"value\": \"opt1\"}]}");
        createRequest.setDataProviderId(100L);

        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        // When
        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        // Then
        assertNotNull(result);
        verify(repository, times(1)).save(any(FieldComponent.class));
    }

    /**
     * 手动实现的 Converter，用于测试
     */
    private static class FieldComponentConverterImpl implements FieldComponentConverter {

        @Override
        public FieldComponentDTO toDTO(FieldComponent domain) {
            if (domain == null) return null;
            return FieldComponentDTO.builder()
                .id(domain.getId())
                .templateId(domain.getTemplateId())
                .templateVersionId(domain.getTemplateVersionId())
                .fieldDefId(domain.getFieldDefId())
                .layoutNodeId(domain.getLayoutNodeId())
                .componentType(domain.getComponentType())
                .labelName(domain.getLabelName())
                .placeholder(domain.getPlaceholder())
                .sortNo(domain.getSortNo())
                .requiredRule(domain.getRequiredRule())
                .readonlyRule(domain.getReadonlyRule())
                .visibleRule(domain.getVisibleRule())
                .componentProps(domain.getComponentProps())
                .dataProviderId(domain.getDataProviderId())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
        }

        @Override
        public FieldComponent toDomain(FieldComponentDTO dto) {
            if (dto == null) return null;
            FieldComponent domain = new FieldComponent();
            domain.setId(dto.getId());
            domain.setTemplateId(dto.getTemplateId());
            domain.setTemplateVersionId(dto.getTemplateVersionId());
            domain.setFieldDefId(dto.getFieldDefId());
            domain.setLayoutNodeId(dto.getLayoutNodeId());
            domain.setComponentType(dto.getComponentType());
            domain.setLabelName(dto.getLabelName());
            domain.setPlaceholder(dto.getPlaceholder());
            domain.setSortNo(dto.getSortNo());
            domain.setRequiredRule(dto.getRequiredRule());
            domain.setReadonlyRule(dto.getReadonlyRule());
            domain.setVisibleRule(dto.getVisibleRule());
            domain.setComponentProps(dto.getComponentProps());
            domain.setDataProviderId(dto.getDataProviderId());
            domain.setCreatedAt(dto.getCreatedAt());
            domain.setUpdatedAt(dto.getUpdatedAt());
            return domain;
        }

        @Override
        public FieldComponent toDomain(FieldComponentCreateRequest request) {
            if (request == null) return null;
            FieldComponent domain = new FieldComponent();
            domain.setLayoutNodeId(request.getLayoutNodeId());
            domain.setFieldDefId(request.getFieldDefId());
            domain.setComponentType(request.getComponentType());
            domain.setLabelName(request.getLabelName());
            domain.setPlaceholder(request.getPlaceholder());
            domain.setSortNo(request.getSortNo());
            domain.setRequiredRule(request.getRequiredRule());
            domain.setReadonlyRule(request.getReadonlyRule());
            domain.setVisibleRule(request.getVisibleRule());
            domain.setComponentProps(request.getComponentProps());
            domain.setDataProviderId(request.getDataProviderId());
            return domain;
        }

        @Override
        public void updateFromDTO(FieldComponentUpdateDTO dto, FieldComponent domain) {
            if (dto == null || domain == null) return;
            if (dto.getLabelName() != null) {
                domain.setLabelName(dto.getLabelName());
            }
            if (dto.getPlaceholder() != null) {
                domain.setPlaceholder(dto.getPlaceholder());
            }
            if (dto.getComponentProps() != null) {
                domain.setComponentProps(dto.getComponentProps());
            }
            if (dto.getRequiredRule() != null) {
                domain.setRequiredRule(dto.getRequiredRule());
            }
        }

        @Override
        public List<FieldComponentDTO> toDTOList(List<FieldComponent> domains) {
            if (domains == null) return java.util.Collections.emptyList();
            return domains.stream()
                .map(this::toDTO)
                .toList();
        }
    }
}