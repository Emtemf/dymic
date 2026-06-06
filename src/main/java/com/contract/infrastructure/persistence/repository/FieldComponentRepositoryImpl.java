package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.infrastructure.persistence.convert.EntityFieldComponentConverter;
import com.contract.infrastructure.persistence.mapper.FieldComponentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FieldComponentRepositoryImpl implements FieldComponentRepository {
    private final FieldComponentMapper mapper;
    private final EntityFieldComponentConverter converter;

    @Override
    public FieldComponent save(FieldComponent component) {
        FieldComponentEntity entity = converter.toEntity(component);
        mapper.insert(entity);
        component.setId(entity.getId());
        return component;
    }

    @Override
    public FieldComponent findById(Long id) {
        FieldComponentEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<FieldComponent> findByVersionId(Long versionId) {
        LambdaQueryWrapper<FieldComponentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldComponentEntity::getTemplateVersionId, versionId);
        List<FieldComponentEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public List<FieldComponent> findByFieldDefId(Long fieldDefId) {
        LambdaQueryWrapper<FieldComponentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldComponentEntity::getFieldDefId, fieldDefId);
        List<FieldComponentEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public List<FieldComponent> findByLayoutNodeId(Long layoutNodeId) {
        LambdaQueryWrapper<FieldComponentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FieldComponentEntity::getLayoutNodeId, layoutNodeId);
        List<FieldComponentEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(FieldComponent component) {
        FieldComponentEntity entity = converter.toEntity(component);
        mapper.updateById(entity);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}