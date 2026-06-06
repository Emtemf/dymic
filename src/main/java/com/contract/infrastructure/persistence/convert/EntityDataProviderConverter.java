package com.contract.infrastructure.persistence.convert;

import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import com.contract.domain.template.DataProvider;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

/**
 * 数据提供方 Entity <-> Domain 转换器
 */
@Mapper(componentModel = "spring")
public interface EntityDataProviderConverter {
    DataProvider toDomain(DataProviderEntity entity);
    DataProviderEntity toEntity(DataProvider domain);
    List<DataProvider> toDomainList(List<DataProviderEntity> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDomain(DataProvider domain, @MappingTarget DataProviderEntity entity);
}