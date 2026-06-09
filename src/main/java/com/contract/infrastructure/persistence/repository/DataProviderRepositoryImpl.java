package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.convert.EntityDataProviderConverter;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import com.contract.infrastructure.persistence.mapper.DataProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 数据提供方仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DataProviderRepositoryImpl implements DataProviderRepository {
    private final DataProviderMapper mapper;
    private final EntityDataProviderConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public DataProvider save(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insertDataProvider(entity);
        return findById(entity.getId());
    }

    @Override
    public DataProvider findById(Long id) {
        DataProviderEntity entity = mapper.selectByIdValue(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public DataProvider findByProviderCode(String providerCode) {
        DataProviderEntity entity = mapper.selectByProviderCode(providerCode);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public boolean existsByProviderCode(String providerCode) {
        return mapper.countByProviderCode(providerCode) > 0;
    }

    @Override
    public void update(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        mapper.updateDataProvider(entity);
    }

    @Override
    public List<DataProvider> findAll() {
        return converter.toDomainList(mapper.selectAllDataProviders());
    }

    @Override
    public List<DataProvider> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return converter.toDomainList(mapper.selectByIds(ids));
    }

    @Override
    public List<DataProvider> findByType(String providerType) {
        return converter.toDomainList(mapper.selectByType(providerType));
    }

    @Override
    public List<DataProvider> findByCategory(String category) {
        return converter.toDomainList(mapper.selectByCategory(category));
    }

    @Override
    public DataProvider findByDictType(String dictType) {
        DataProviderEntity entity = mapper.selectDictByType(dictType);
        return entity != null ? converter.toDomain(entity) : null;
    }
}
