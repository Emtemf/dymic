package com.contract.application.template;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.application.template.dto.DataProviderCreateRequest;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.application.template.convert.FieldComponentConverter;
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
import java.util.Arrays;
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

    /**
     * 需要数据源的组件类型
     */
    private static final Set<String> DATA_SOURCE_COMPONENT_TYPES = Set.of(
        "SELECT",      // 下拉选择框
        "RADIO",       // 单选框
        "CHECKBOX",    // 复选框
        "TREE",        // 树形选择器
        "CASCADE",     // 级联选择器
        "MULTI_SELECT" // 多选下拉框
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
        // 验证组件类型有效性
        if (!FieldComponent.isValidComponentType(request.getComponentType())) {
            throw new BizException("不支持的组件类型: " + request.getComponentType());
        }

        // 验证布局节点存在
        LayoutNode node = layoutNodeRepository.findById(request.getLayoutNodeId());
        if (node == null) {
            throw new BizException("布局节点不存在: " + request.getLayoutNodeId());
        }

        // 验证字段定义存在
        FieldDef fieldDef = fieldDefRepository.findById(request.getFieldDefId());
        if (fieldDef == null) {
            throw new BizException("字段定义不存在: " + request.getFieldDefId());
        }

        // 创建字段组件绑定
        FieldComponent component = converter.toDomain(request);
        component.setTemplateId(templateId);
        component.setTemplateVersionId(versionId);
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());

        // 默认labelName使用fieldNameCn
        if (component.getLabelName() == null) {
            component.setLabelName(fieldDef.getFieldNameCn());
        }

        // 默认sortNo为0
        if (component.getSortNo() == null) {
            component.setSortNo(0);
        }

        // 处理数据源绑定（业务友好的自动创建逻辑）
        if (needsDataProvider(request.getComponentType())) {
            Long dataProviderId = handleDataProvider(request);
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
     * - STATIC: 业务自定义选项 → 自动创建临时DataProvider
     * - DICT: 字典数据 → 查询或创建字典DataProvider
     * - HTTP/PLATFORM/INTERNAL: IT已配置 → 使用已存在的DataProvider
     *
     * @param request 字段组件创建请求
     * @return DataProvider ID
     */
    private Long handleDataProvider(FieldComponentCreateRequest request) {
        String dataSourceType = request.getDataSourceType();

        // 如果没有指定数据源类型，返回null（不绑定数据源）
        if (dataSourceType == null || dataSourceType.isEmpty()) {
            log.warn("组件类型 {} 需要数据源，但未指定dataSourceType", request.getComponentType());
            return null;
        }

        // 构建DataProviderCreateRequest
        DataProviderCreateRequest dpRequest = DataProviderCreateRequest.builder()
            .dataSourceType(dataSourceType)
            .displayName(request.getLabelName() + "-数据源")
            .staticOptionsJson(request.getStaticOptionsJson())
            .dictType(request.getDictType())
            .dataProviderId(request.getDataProviderId())
            .build();

        // 调用DataProviderService处理
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