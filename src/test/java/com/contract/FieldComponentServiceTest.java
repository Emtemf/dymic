package com.contract;

import com.contract.application.template.DataProviderService;
import com.contract.application.template.DataSourceConfigService;
import com.contract.application.template.FieldComponentService;
import com.contract.application.template.convert.FieldComponentConverter;
import com.contract.application.template.convert.FieldComponentConverterImpl;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
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
import static org.mockito.ArgumentMatchers.argThat;
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

    @Mock
    private DataProviderService dataProviderService;

    @Mock
    private DataSourceConfigService dataSourceConfigService;

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
        layoutNode = new LayoutNode();
        layoutNode.setId(1L);
        layoutNode.setTemplateId(100L);
        layoutNode.setTemplateVersionId(200L);
        layoutNode.setNodeCode("card_test");
        layoutNode.setNodeName("测试卡片");
        layoutNode.setNodeType("CARD");

        fieldDef = new FieldDef();
        fieldDef.setId(1L);
        fieldDef.setTemplateId(100L);
        fieldDef.setTemplateVersionId(200L);
        fieldDef.setFieldCode("contractName");
        fieldDef.setFieldNameCn("合同名称");
        fieldDef.setFieldNameEn("Contract Name");
        fieldDef.setDataType("STRING");

        createRequest = FieldComponentCreateRequest.builder()
            .layoutNodeId(1L)
            .fieldDefId(1L)
            .componentType("INPUT")
            .placeholder("请输入合同名称")
            .sortNo(1)
            .build();

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
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        assertNotNull(result);
        assertEquals("INPUT", result.getComponentType());
        assertEquals(1L, result.getLayoutNodeId());
        assertEquals(1L, result.getFieldDefId());
        verify(repository, times(1)).save(any(FieldComponent.class));
    }

    @Test
    void testCreate_shouldRouteStaticSourceToBusinessConfigService() {
        createRequest.setComponentType("SELECT");
        createRequest.setLabelName("地区");
        createRequest.setDataSourceType("STATIC");
        createRequest.setStaticOptionsJson("[{\"value\":\"BJ\",\"label\":\"北京\"}]");

        DataProviderDTO businessProvider = new DataProviderDTO();
        businessProvider.setId(99L);
        DataProviderDTO itProvider = new DataProviderDTO();
        itProvider.setId(88L);

        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(dataSourceConfigService.create(any())).thenReturn(businessProvider);
        when(repository.save(any(FieldComponent.class))).thenAnswer(invocation -> {
            FieldComponent component = invocation.getArgument(0);
            component.setId(1L);
            return component;
        });

        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        assertNotNull(result);
        assertEquals(99L, result.getDataProviderId());
        verify(dataSourceConfigService).create(argThat((BusinessDataSourceRequest request) ->
            "STATIC".equals(request.getProviderType()) && request.getConfigJson().contains("BJ")
        ));
        verify(dataProviderService, never()).createFromBusinessRequest(any());
    }

    @Test
    void testCreate_shouldUseFieldNameAsDefaultLabel() {
        createRequest.setLabelName(null);
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        assertNotNull(result);
        assertEquals("合同名称", result.getLabelName());
    }

    @Test
    void testCreate_shouldUseCustomLabelIfProvided() {
        createRequest.setLabelName("自定义标签");
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenAnswer(invocation -> {
            FieldComponent comp = invocation.getArgument(0);
            comp.setId(1L);
            return comp;
        });

        FieldComponentDTO result = service.create(100L, 200L, createRequest);

        assertNotNull(result);
        assertEquals("自定义标签", result.getLabelName());
    }

    @Test
    void testCreate_shouldValidateLayoutNodeExists() {
        when(layoutNodeRepository.findById(999L)).thenReturn(null);
        createRequest.setLayoutNodeId(999L);

        assertThrows(BizException.class, () -> service.create(100L, 200L, createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_shouldValidateFieldDefExists() {
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(999L)).thenReturn(null);
        createRequest.setFieldDefId(999L);

        assertThrows(BizException.class, () -> service.create(100L, 200L, createRequest));
        verify(repository, never()).save(any());
    }

    @Test
    void testCreate_shouldValidateComponentType() {
        createRequest.setComponentType("INVALID_TYPE");

        assertThrows(BizException.class, () -> service.create(100L, 200L, createRequest));
        verify(repository, never()).save(any());
        verify(layoutNodeRepository, never()).findById(any());
        verify(fieldDefRepository, never()).findById(any());
    }

    @Test
    void testCreate_shouldSupportAllValidComponentTypes() {
        when(layoutNodeRepository.findById(1L)).thenReturn(layoutNode);
        when(fieldDefRepository.findById(1L)).thenReturn(fieldDef);
        when(repository.save(any(FieldComponent.class))).thenReturn(fieldComponent);

        String[] validTypes = {"INPUT", "SELECT", "DATE", "DATETIME", "MONEY", "NUMBER",
            "RADIO", "CHECKBOX", "TREE", "UPLOAD", "TEXTAREA", "RICHTEXT",
            "SWITCH", "SLIDER", "RATE", "COLOR", "CASCADE"};

        for (String type : validTypes) {
            createRequest.setComponentType(type);
            if ("SELECT".equals(type) || "RADIO".equals(type) || "CHECKBOX".equals(type) || "TREE".equals(type) || "CASCADE".equals(type)) {
                createRequest.setDataSourceType(null);
            }
            FieldComponentDTO result = service.create(100L, 200L, createRequest);
            assertNotNull(result);
        }
    }

    @Test
    void testGetById_shouldReturnComponent() {
        when(repository.findById(1L)).thenReturn(fieldComponent);

        FieldComponentDTO result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("INPUT", result.getComponentType());
    }

    @Test
    void testGetById_shouldThrowExceptionWhenNotFound() {
        when(repository.findById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> service.getById(999L));
    }

    @Test
    void testListByVersionId_shouldReturnList() {
        FieldComponent comp1 = new FieldComponent();
        comp1.setId(1L);
        comp1.setTemplateVersionId(200L);
        comp1.setComponentType("INPUT");

        FieldComponent comp2 = new FieldComponent();
        comp2.setId(2L);
        comp2.setTemplateVersionId(200L);
        comp2.setComponentType("SELECT");

        when(repository.findByVersionId(200L)).thenReturn(Arrays.asList(comp1, comp2));

        List<FieldComponentDTO> result = service.listByVersionId(200L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testListByLayoutNodeId_shouldReturnList() {
        FieldComponent comp1 = new FieldComponent();
        comp1.setId(1L);
        comp1.setLayoutNodeId(1L);
        comp1.setComponentType("INPUT");

        when(repository.findByLayoutNodeId(1L)).thenReturn(Arrays.asList(comp1));

        List<FieldComponentDTO> result = service.listByLayoutNodeId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdate_shouldUpdateComponent() {
        FieldComponentUpdateDTO updateDTO = new FieldComponentUpdateDTO();
        updateDTO.setLabelName("更新后的标签");
        updateDTO.setPlaceholder("更新后的提示");

        when(repository.findById(1L)).thenReturn(fieldComponent);
        doNothing().when(repository).update(any(FieldComponent.class));

        FieldComponentDTO result = service.update(1L, updateDTO);

        assertNotNull(result);
        assertEquals("更新后的标签", result.getLabelName());
        assertEquals("更新后的提示", result.getPlaceholder());
        verify(repository, times(1)).update(any());
    }

    @Test
    void testUpdate_shouldThrowExceptionWhenNotFound() {
        FieldComponentUpdateDTO updateDTO = new FieldComponentUpdateDTO();
        updateDTO.setLabelName("更新后的标签");

        when(repository.findById(999L)).thenReturn(null);

        assertThrows(BizException.class, () -> service.update(999L, updateDTO));
        verify(repository, never()).update(any());
    }
}
