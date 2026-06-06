package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.FieldComponentEntity;
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
public interface EntityFieldComponentConverter {
    @Mapping(target = "componentProps", expression = "java(mapToJson(domain.getComponentProps()))")
    @Mapping(target = "requiredRule", expression = "java(mapToJson(domain.getRequiredRule()))")
    @Mapping(target = "visibleRule", expression = "java(mapToJson(domain.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(mapToJson(domain.getReadonlyRule()))")
    FieldComponent toDomain(FieldComponentEntity entity);

    @Mapping(target = "componentProps", expression = "java(jsonToMap(entity.getComponentProps()))")
    @Mapping(target = "requiredRule", expression = "java(jsonToMap(entity.getRequiredRule()))")
    @Mapping(target = "visibleRule", expression = "java(jsonToMap(entity.getVisibleRule()))")
    @Mapping(target = "readonlyRule", expression = "java(jsonToMap(entity.getReadonlyRule()))")
    FieldComponentEntity toEntity(FieldComponent domain);

    List<FieldComponent> toDomainList(List<FieldComponentEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDomain(FieldComponent domain, @MappingTarget FieldComponentEntity entity);

    // Custom conversion methods for JSONB fields
    default String mapToJson(Map<String, Object> map) {
        return JsonbUtils.toJson(map);
    }

    default Map<String, Object> jsonToMap(String json) {
        return JsonbUtils.fromJson(json, Map.class);
    }
}