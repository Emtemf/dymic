package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.FieldDefEntity;
import com.contract.domain.template.FieldDef;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.infrastructure.persistence.convert.EntityFieldDefConverter;
import com.contract.infrastructure.persistence.mapper.FieldDefMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 字段定义仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FieldDefRepositoryImpl implements FieldDefRepository {
    private final FieldDefMapper mapper;
    private final EntityFieldDefConverter converter;

    @Override
    public FieldDef save(FieldDef fieldDef) {
        FieldDefEntity entity = converter.toEntity(fieldDef);
        mapper.insert(entity);
        fieldDef.setId(entity.getId());
        return fieldDef;
    }

    @Override
    public FieldDef findById(Long id) {
        FieldDefEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<FieldDef> findByVersionId(Long versionId) {
        LambdaQueryWrapper<FieldDefEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldDefEntity::getTemplateVersionId, versionId);
        List<FieldDefEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(FieldDef fieldDef) {
        FieldDefEntity entity = converter.toEntity(fieldDef);
        mapper.updateById(entity);
    }

    @Override
    public boolean existsByFieldCode(String fieldCode) {
        LambdaQueryWrapper<FieldDefEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldDefEntity::getFieldCode, fieldCode);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByFieldPath(String fieldPath) {
        LambdaQueryWrapper<FieldDefEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldDefEntity::getFieldPath, fieldPath);
        return mapper.selectCount(wrapper) > 0;
    }
}