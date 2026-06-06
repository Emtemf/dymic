package com.contract.application.template;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.application.template.convert.FieldComponentConverter;
import com.contract.common.util.JsonbUtils;
import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段组件绑定应用服务
 */
@Service
@RequiredArgsConstructor
public class FieldComponentService {
    private final FieldComponentRepository repository;
    private final FieldComponentConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 创建字段组件绑定(由 FieldDefService 调用)
     */
    public FieldComponentDTO create(
        Long templateId,
        Long versionId,
        Long fieldDefId,
        Long layoutNodeId,
        FieldDefCreateDTO dto
    ) {
        FieldComponent component = new FieldComponent();
        component.setId(idGenerator.nextId());
        component.setTemplateId(templateId);
        component.setTemplateVersionId(versionId);
        component.setFieldDefId(fieldDefId);
        component.setLayoutNodeId(layoutNodeId);
        component.setComponentType(dto.getComponentType());
        component.setLabelName(dto.getDisplayName());
        component.setPlaceholder(dto.getPlaceholder());
        component.setSortNo(dto.getSortNo());
        component.setCreatedAt(LocalDateTime.now());
        component.setUpdatedAt(LocalDateTime.now());

        // 处理数据来源配置(针对 SELECT 类型)
        if ("SELECT".equals(dto.getComponentType())) {
            Map<String, Object> props = new HashMap<>();

            if ("STATIC".equals(dto.getDataSourceType())) {
                // 静态选项
                props.put("options", dto.getStaticOptions());
                component.setComponentProps(JsonbUtils.toJson(props));
            } else if ("PROVIDER".equals(dto.getDataSourceType())) {
                // 数据提供方
                component.setDataProviderId(dto.getDataProviderId());
                props.put("dataProviderId", dto.getDataProviderId());
                component.setComponentProps(JsonbUtils.toJson(props));
            }
        }

        // 处理必填规则
        if (dto.getRequired() != null && dto.getRequired()) {
            Map<String, Object> requiredRule = new HashMap<>();
            requiredRule.put("required", true);
            component.setRequiredRule(JsonbUtils.toJson(requiredRule));
        }

        repository.save(component);
        return converter.toDTO(component);
    }

    @Transactional
    public FieldComponentDTO update(Long id, FieldComponentUpdateDTO dto) {
        FieldComponent component = repository.findById(id);
        if (component == null) {
            throw new RuntimeException("字段组件绑定不存在：" + id);
        }

        if (dto.getLabelName() != null) {
            component.setLabelName(dto.getLabelName());
        }
        if (dto.getPlaceholder() != null) {
            component.setPlaceholder(dto.getPlaceholder());
        }
        if (dto.getComponentProps() != null) {
            component.setComponentProps(JsonbUtils.toJson(dto.getComponentProps()));
        }
        if (dto.getRequiredRule() != null) {
            component.setRequiredRule(JsonbUtils.toJson(dto.getRequiredRule()));
        }

        component.setUpdatedAt(LocalDateTime.now());
        repository.update(component);
        return converter.toDTO(component);
    }

    public FieldComponentDTO getById(Long id) {
        FieldComponent component = repository.findById(id);
        if (component == null) {
            throw new RuntimeException("字段组件绑定不存在：" + id);
        }
        return converter.toDTO(component);
    }
}