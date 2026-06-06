package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.FieldComponentEntity;
import com.contract.domain.template.FieldComponent;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityFieldComponentConverter {
    FieldComponent toDomain(FieldComponentEntity entity);
    FieldComponentEntity toEntity(FieldComponent domain);
    List<FieldComponent> toDomainList(List<FieldComponentEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDomain(FieldComponent domain, @MappingTarget FieldComponentEntity entity);
}