package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.FieldComponentEntity;
import com.contract.domain.template.FieldComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityFieldComponentConverter {
    FieldComponent toDomain(FieldComponentEntity entity);
    FieldComponentEntity toEntity(FieldComponent domain);
    List<FieldComponent> toDomainList(List<FieldComponentEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @NullValuePropertyMappingStrategy(NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(FieldComponent domain, @MappingTarget FieldComponentEntity entity);
}