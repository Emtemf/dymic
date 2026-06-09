package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.FieldComponent;
import com.contract.domain.template.repository.FieldComponentRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.convert.EntityFieldComponentConverter;
import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import com.contract.infrastructure.persistence.mapper.FieldComponentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FieldComponentRepositoryImpl implements FieldComponentRepository {
    private final FieldComponentMapper mapper;
    private final EntityFieldComponentConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public FieldComponent save(FieldComponent component) {
        FieldComponentEntity entity = converter.toEntity(component);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insertFieldComponent(entity);
        component.setId(entity.getId());
        return component;
    }

    @Override
    public FieldComponent findById(Long id) {
        FieldComponentEntity entity = mapper.selectByIdValue(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<FieldComponent> findByVersionId(Long versionId) {
        List<FieldComponentEntity> entities = mapper.selectByVersionId(versionId);
        return converter.toDomainList(entities);
    }

    @Override
    public List<FieldComponent> findByFieldDefId(Long fieldDefId) {
        List<FieldComponentEntity> entities = mapper.selectByFieldDefId(fieldDefId);
        return converter.toDomainList(entities);
    }

    @Override
    public List<FieldComponent> findByLayoutNodeId(Long layoutNodeId) {
        List<FieldComponentEntity> entities = mapper.selectByLayoutNodeId(layoutNodeId);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(FieldComponent component) {
        FieldComponentEntity entity = converter.toEntity(component);
        mapper.updateFieldComponent(entity);
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteByIdValue(id);
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        mapper.physicalDeleteByVersionId(versionId);
    }
}
