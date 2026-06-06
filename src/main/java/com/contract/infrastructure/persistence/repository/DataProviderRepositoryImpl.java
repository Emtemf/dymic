package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import com.contract.infrastructure.persistence.mapper.DataProviderMapper;
import com.contract.domain.template.DataProvider;
import com.contract.domain.template.repository.DataProviderRepository;
import com.contract.infrastructure.persistence.convert.EntityDataProviderConverter;
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

    @Override
    public DataProvider save(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        mapper.insert(entity);
        provider.setId(entity.getId());
        return provider;
    }

    @Override
    public DataProvider findById(Long id) {
        DataProviderEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public DataProvider findByProviderCode(String providerCode) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderCode, providerCode);
        DataProviderEntity entity = mapper.selectOne(wrapper);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public boolean existsByProviderCode(String providerCode) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderCode, providerCode);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public void update(DataProvider provider) {
        DataProviderEntity entity = converter.toEntity(provider);
        mapper.updateById(entity);
    }

    @Override
    public List<DataProvider> findAll() {
        List<DataProviderEntity> entities = mapper.selectList(null);
        return converter.toDomainList(entities);
    }

    @Override
    public List<DataProvider> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<DataProviderEntity> entities = mapper.selectBatchIds(ids);
        return converter.toDomainList(entities);
    }

    @Override
    public List<DataProvider> findByType(String providerType) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderType, providerType)
               .orderByDesc(DataProviderEntity::getCreatedAt);
        List<DataProviderEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public DataProvider findByDictType(String dictType) {
        LambdaQueryWrapper<DataProviderEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DataProviderEntity::getProviderType, "DICT")
               .likeRight(DataProviderEntity::getConfigJson, "{\"dictType\":\"" + dictType + "\"");
        DataProviderEntity entity = mapper.selectOne(wrapper);
        return entity != null ? converter.toDomain(entity) : null;
    }
}