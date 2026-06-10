package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.LayoutNode;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
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
 * LayoutNode Entity ↔ Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityLayoutNodeConverter {
    LayoutNode toDomain(LayoutNodeEntity entity);
    LayoutNodeEntity toEntity(LayoutNode domain);
    List<LayoutNode> toDomainList(List<LayoutNodeEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(LayoutNode domain, @MappingTarget LayoutNodeEntity entity);

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
