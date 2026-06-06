package com.contract.application.template.convert;

import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.domain.template.LayoutNode;
import com.contract.common.util.JsonbUtils;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import java.util.Map;

/**
 * LayoutNode Domain ↔ DTO 转换器
 */
@Mapper(componentModel = "spring")
public interface LayoutNodeConverter {
    /**
     * Domain → DTO
     */
    @Mapping(target = "visibleRule", expression = "java(mapObjectToMap(domain.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(mapObjectToMap(domain.getReadonlyRule()))")
    @Mapping(target = "propsJson", expression = "java(mapObjectToMap(domain.getPropsJson()))")
    LayoutNodeDTO toDTO(LayoutNode domain);

    /**
     * Domain List → DTO List
     */
    List<LayoutNodeDTO> toDTOList(List<LayoutNode> domains);

    /**
     * CreateDTO → Domain
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "nodeCode", ignore = true)
    @Mapping(target = "nodePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "visibleRule", expression = "java(dto.getVisibleRule())")
    @Mapping(target = "readonlyRule", expression = "java(dto.getReadonlyRule())")
    @Mapping(target = "propsJson", expression = "java(dto.getPropsJson())")
    LayoutNode toDomain(LayoutNodeCreateDTO dto);

    /**
     * 更新 Domain（部分更新）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "templateVersionId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "nodeCode", ignore = true)
    @Mapping(target = "nodeType", ignore = true)
    @Mapping(target = "nodePath", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "visibleRule", expression = "java(dto.getVisibleRule())")
    @Mapping(target = "readonlyRule", expression = "java(dto.getReadonlyRule())")
    @Mapping(target = "propsJson", expression = "java(dto.getPropsJson())")
    void updateDomainFromDTO(LayoutNodeUpdateDTO dto, @MappingTarget LayoutNode domain);

    /**
     * Object → Map 转换（用于 JSONB 字段）
     */
    default Map<String, Object> mapObjectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        if (obj instanceof String) {
            return JsonbUtils.fromJson((String) obj, Map.class);
        }
        return null;
    }
}