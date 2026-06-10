package com.contract.application.template;

import com.contract.application.template.convert.FieldComponentConverter;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderCreateRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 字段组件绑定应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FieldComponentService {
    private final FieldComponentRepository repository;
    private final FieldComponentConverter converter;
    private final LayoutNodeRepository layoutNodeRepository;
    private final FieldDefRepository fieldDefRepository;
    private final DataProviderService dataProviderService;
    private final DataSourceConfigService dataSourceConfigService;

    /**
     * 需要数据源的组件类型
     */
    private static final Set<String> DATA_SOURCE_COMPONENT_TYPES = Set.of(
        "SELECT",
        "RADIO",
        "CHECKBOX",
        "TREE",
        "CASCADE",
        "MULTI_SELECT"
    );

    /**
     * 创建字段组件绑定（支持业务友好的数据源自动创建）
     *
     * @param templateId 模板ID
     * @param versionId  版本ID
     * @param request    创建请求
     * @return 创建的字段组件绑定DTO
     */
    @Transactional
    public FieldComponentDTO create(Long templateId, Long versionId, FieldComponentCreateRequest request) {
        if (!FieldComponent.isValidComponentType(request.getComponentType())) {
            throw new BizException("不支持的组件类型: " + request.getComponentType());
        }

        LayoutNode node = layoutNodeRepository.findById(request.getLayoutNodeId());
        if (node == null) {
            throw new BizException("布局节点不存在: " + request.getLayoutNodeId());
        }

        FieldDef fieldDef = fieldDefRepository.findById(request.getFieldDefId());
        if (fieldDef == null) {
            throw new BizException("字段定义不存在: " + request.getFieldDefId());
        }

        FieldComponent component = converter.toDomain(request);
        component.setTemplateId(templateId);
        component.setTemplateVersionId(versionId);
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());

        if (component.getLabelName() == null) {
            component.setLabelName(fieldDef.getFieldNameCn());
        }

        if (component.getSortNo() == null) {
            component.setSortNo(0);
        }

        if (needsDataProvider(request.getComponentType())) {
            Long dataProviderId = handleDataProvider(request, fieldDef);
            component.setDataProviderId(dataProviderId);
        }

        FieldComponent saved = repository.save(component);
        log.info("创建字段组件绑定成功: id={}, templateId={}, versionId={}, componentType={}, dataProviderId={}",
            saved.getId(), templateId, versionId, saved.getComponentType(), saved.getDataProviderId());

        return converter.toDTO(saved);
    }

    /**
     * 判断组件是否需要数据源
     *
     * @param componentType 组件类型
     * @return 是否需要数据源
     */
    private boolean needsDataProvider(String componentType) {
        return DATA_SOURCE_COMPONENT_TYPES.contains(componentType.toUpperCase());
    }

    /**
     * 处理数据源绑定（核心逻辑）
     *
     * 根据业务选择的数据源类型自动创建或查找DataProvider：
     * - STATIC: 业务自定义选项 → 走业务配置服务
     * - DICT: 字典数据 → 走业务配置服务
     * - HTTP/PLATFORM/INTERNAL: IT已配置 → 使用已存在的DataProvider
     *
     * @param request 字段组件创建请求
     * @param fieldDef 字段定义
     * @return DataProvider ID
     */
    private Long handleDataProvider(FieldComponentCreateRequest request, FieldDef fieldDef) {
        String dataSourceType = request.getDataSourceType();

        if (dataSourceType == null || dataSourceType.isEmpty()) {
            log.warn("组件类型 {} 需要数据源，但未指定dataSourceType", request.getComponentType());
            return null;
        }

        String providerName = request.getLabelName() != null && !request.getLabelName().isBlank()
            ? request.getLabelName() + "-数据源"
            : fieldDef.getFieldNameCn() + "-数据源";

        if ("STATIC".equals(dataSourceType)) {
            BusinessDataSourceRequest businessRequest = BusinessDataSourceRequest.builder()
                .providerName(providerName)
                .providerType("STATIC")
                .configJson(request.getStaticOptionsJson())
                .build();
            return dataSourceConfigService.create(businessRequest).getId();
        }

        if ("DICT".equals(dataSourceType)) {
            BusinessDataSourceRequest businessRequest = BusinessDataSourceRequest.builder()
                .providerName(providerName)
                .providerType("DICT")
                .configJson("{\"dictType\":\"" + request.getDictType() + "\"}")
                .build();
            return dataSourceConfigService.create(businessRequest).getId();
        }

        DataProviderCreateRequest dpRequest = DataProviderCreateRequest.builder()
            .dataSourceType(dataSourceType)
            .displayName(providerName)
            .staticOptionsJson(request.getStaticOptionsJson())
            .dictType(request.getDictType())
            .dataProviderId(request.getDataProviderId())
            .build();

        DataProviderDTO providerDTO = dataProviderService.createFromBusinessRequest(dpRequest);
        return providerDTO.getId();
    }

    /**
     * 创建字段组件绑定(由 FieldDefService 调用，不验证布局节点和字段定义)
     *
     * @param templateId    模板ID
     * @param versionId     版本ID
     * @param fieldDefId    字段定义ID
     * @param layoutNodeId  布局节点ID
     * @param componentType 组件类型
     * @param labelName     显示名称
     * @param placeholder   输入提示
     * @param sortNo        排序号
     * @return 创建的字段组件绑定DTO
     */
    @Transactional
    public FieldComponentDTO createFromFieldDef(
        Long templateId,
        Long versionId,
        Long fieldDefId,
        Long layoutNodeId,
        String componentType,
        String labelName,
        String placeholder,
        Integer sortNo
    ) {
        FieldComponent component = new FieldComponent();
        component.setTemplateId(templateId);
        component.setTemplateVersionId(versionId);
        component.setFieldDefId(fieldDefId);
        component.setLayoutNodeId(layoutNodeId);
        component.setComponentType(componentType);
        component.setLabelName(labelName);
        component.setPlaceholder(placeholder);
        component.setSortNo(sortNo != null ? sortNo : 0);
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());

        FieldComponent saved = repository.save(component);
        return converter.toDTO(saved);
    }

    /**
     * 根据ID查询字段组件绑定
     *
     * @param id 字段组件绑定ID
     * @return 字段组件绑定DTO
     */
    public FieldComponentDTO getById(Long id) {
        FieldComponent component = repository.findById(id);
        if (component == null) {
            throw new BizException("字段组件绑定不存在：" + id);
        }
        return converter.toDTO(component);
    }

    /**
     * 根据版本ID查询字段组件绑定列表
     *
     * @param versionId 版本ID
     * @return 字段组件绑定列表
     */
    public List<FieldComponentDTO> listByVersionId(Long versionId) {
        List<FieldComponent> components = repository.findByVersionId(versionId);
        return converter.toDTOList(components);
    }

    /**
     * 根据布局节点ID查询字段组件绑定列表
     *
     * @param layoutNodeId 布局节点ID
     * @return 字段组件绑定列表
     */
    public List<FieldComponentDTO> listByLayoutNodeId(Long layoutNodeId) {
        List<FieldComponent> components = repository.findByLayoutNodeId(layoutNodeId);
        return converter.toDTOList(components);
    }

    /**
     * 更新字段组件绑定
     *
     * @param id      字段组件绑定ID
     * @param request 更新请求
     * @return 更新后的字段组件绑定DTO
     */
    @Transactional
    public FieldComponentDTO update(Long id, FieldComponentUpdateDTO request) {
        FieldComponent component = repository.findById(id);
        if (component == null) {
            throw new BizException("字段组件绑定不存在：" + id);
        }

        converter.updateFromDTO(request, component);
        component.setUpdatedAt(LocalDateTime.now());

        repository.update(component);
        log.info("更新字段组件绑定成功: id={}", id);

        return converter.toDTO(component);
    }

    /**
     * 删除字段组件绑定
     *
     * @param id 字段组件绑定ID
     */
    @Transactional
    public void delete(Long id) {
        FieldComponent component = repository.findById(id);
        if (component == null) {
            throw new BizException("字段组件绑定不存在：" + id);
        }

        repository.deleteById(id);
        log.info("删除字段组件绑定成功: id={}", id);
    }

    @Transactional
    public void deleteByVersionId(Long versionId) {
        repository.deleteByTemplateVersionId(versionId);
    }
}
