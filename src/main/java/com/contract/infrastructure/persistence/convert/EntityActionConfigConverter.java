package com.contract.infrastructure.persistence.convert;

import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import com.contract.domain.template.ActionConfig;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * ActionConfig Entity <-> Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityActionConfigConverter {

    /**
     * Entity -> Domain
     */
    ActionConfig toDomain(ActionConfigEntity entity);

    /**
     * Domain -> Entity
     */
    ActionConfigEntity toEntity(ActionConfig domain);

    /**
     * Entity List -> Domain List
     */
    List<ActionConfig> toDomainList(List<ActionConfigEntity> entities);

    /**
     * 更新 Entity（部分更新）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDomain(ActionConfig domain, @MappingTarget ActionConfigEntity entity);
}