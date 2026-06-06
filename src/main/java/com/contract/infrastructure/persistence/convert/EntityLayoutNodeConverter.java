package com.contract.infrastructure.persistence.convert;

import com.contract.adapter.persistence.entity.LayoutNodeEntity;
import com.contract.domain.template.LayoutNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * LayoutNode Entity ↔ Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityLayoutNodeConverter {
    /**
     * Entity → Domain
     */
    LayoutNode toDomain(LayoutNodeEntity entity);

    /**
     * Domain → Entity
     */
    LayoutNodeEntity toEntity(LayoutNode domain);

    /**
     * Entity List → Domain List
     */
    List<LayoutNode> toDomainList(List<LayoutNodeEntity> entities);

    /**
     * 更新 Entity（部分更新）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDomain(LayoutNode domain, @MappingTarget LayoutNodeEntity entity);
}