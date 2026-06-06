package com.contract.application.template.convert;

import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.domain.template.FieldComponent;
import com.contract.common.util.JsonbUtils;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface FieldComponentConverter {
    @Mapping(target = "componentProps", expression = "java(mapToJson(domain.getComponentProps()))")
    @Mapping(target = "requiredRule", expression = "java(mapToJson(domain.getRequiredRule()))")
    @Mapping(target = "visibleRule", expression = "java(mapToJson(domain.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(mapToJson(domain.getReadonlyRule()))")
    FieldComponentDTO toDTO(FieldComponent domain);

    @Mapping(target = "componentProps", expression = "java(jsonToMap(dto.getComponentProps()))")
    @Mapping(target = "requiredRule", expression = "java(jsonToMap(dto.getRequiredRule()))")
    @Mapping(target = "visibleRule", expression = "java(jsonToMap(dto.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(jsonToMap(dto.getReadonlyRule()))")
    FieldComponent toDomain(FieldComponentDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "componentProps", expression = "java(jsonToMap(dto.getComponentProps()))")
    @Mapping(target = "requiredRule", expression = "java(jsonToMap(dto.getRequiredRule()))")
    @Mapping(target = "visibleRule", expression = "java(jsonToMap(dto.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(jsonToMap(dto.getReadonlyRule()))")
    void updateDomainFromDTO(FieldComponentDTO dto, @MappingTarget FieldComponent domain);

    List<FieldComponentDTO> toDTOList(List<FieldComponent> domains);

    // Custom conversion methods for JSONB fields
    default String mapToJson(Map<String, Object> map) {
        return JsonbUtils.toJson(map);
    }

    default Map<String, Object> jsonToMap(String json) {
        return JsonbUtils.fromJson(json, Map.class);
    }
}