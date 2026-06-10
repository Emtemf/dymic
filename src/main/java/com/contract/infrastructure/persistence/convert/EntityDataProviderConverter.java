package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.DataProvider;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import org.mapstruct.Mapper;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据提供方 Entity <-> Domain 转换器
 *
 * 注意：DataProvider重构后使用值对象，不能直接用MapStruct映射
 */
@Mapper(componentModel = "spring")
public interface EntityDataProviderConverter {

    /**
     * Entity -> Domain（使用rebuild方法重建领域对象）
     */
    default DataProvider toDomain(DataProviderEntity entity) {
        if (entity == null) {
            return null;
        }
        return DataProvider.rebuild(
            entity.getId(),
            entity.getProviderCode(),
            entity.getProviderName(),
            entity.getProviderType(),
            entity.getConfigJson(),
            entity.getCacheEnabled(),
            entity.getCacheTtlSeconds(),
            entity.getIsTemporary(),
            entity.getStatus(),
            entity.getCreatedBy(),
            entity.getCreatedName(),
            entity.getCreatedAt() != null ? entity.getCreatedAt().withOffsetSameInstant(ZoneOffset.UTC) : null,
            entity.getUpdatedBy(),
            entity.getUpdatedName(),
            entity.getUpdatedAt() != null ? entity.getUpdatedAt().withOffsetSameInstant(ZoneOffset.UTC) : null
        );
    }

    /**
     * Domain -> Entity
     */
    default DataProviderEntity toEntity(DataProvider domain) {
        if (domain == null) {
            return null;
        }
        DataProviderEntity entity = new DataProviderEntity();
        entity.setId(domain.getId());
        entity.setProviderCode(domain.getProviderCode());
        entity.setProviderName(domain.getProviderName());
        entity.setProviderType(domain.getProviderType());
        entity.setDataSourceCategory(domain.getDataSourceCategory());
        entity.setConfigJson(domain.getConfigJson());
        entity.setCacheEnabled(domain.getCacheEnabled());
        entity.setCacheTtlSeconds(domain.getCacheTtlSeconds());
        entity.setIsTemporary(domain.getIsTemporary());
        entity.setStatus(domain.getStatus());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedName(domain.getCreatedName());
        entity.setCreatedAt(domain.getCreatedAt() != null ? domain.getCreatedAt().atOffset(ZoneOffset.UTC) : null);
        entity.setUpdatedBy(domain.getUpdatedBy());
        entity.setUpdatedName(domain.getUpdatedName());
        entity.setUpdatedAt(domain.getUpdatedAt() != null ? domain.getUpdatedAt().atOffset(ZoneOffset.UTC) : null);
        return entity;
    }

    /**
     * 批量转换
     */
    default List<DataProvider> toDomainList(List<DataProviderEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
}
