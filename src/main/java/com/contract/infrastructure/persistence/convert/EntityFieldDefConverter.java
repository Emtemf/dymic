package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.FieldDef;
import com.contract.infrastructure.persistence.entity.FieldDefEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * FieldDef Entity ↔ Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityFieldDefConverter {
    FieldDef toDomain(FieldDefEntity entity);
    FieldDefEntity toEntity(FieldDef domain);
    List<FieldDef> toDomainList(List<FieldDefEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(FieldDef domain, @MappingTarget FieldDefEntity entity);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
