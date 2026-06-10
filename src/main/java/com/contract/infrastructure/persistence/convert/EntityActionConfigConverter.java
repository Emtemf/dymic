package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.ActionConfig;
import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
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
 * ActionConfig Entity <-> Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityActionConfigConverter {
    ActionConfig toDomain(ActionConfigEntity entity);
    ActionConfigEntity toEntity(ActionConfig domain);
    List<ActionConfig> toDomainList(List<ActionConfigEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDomain(ActionConfig domain, @MappingTarget ActionConfigEntity entity);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
