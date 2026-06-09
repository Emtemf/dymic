package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.FieldDef;
import com.contract.domain.template.repository.FieldDefRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.convert.EntityFieldDefConverter;
import com.contract.infrastructure.persistence.entity.FieldDefEntity;
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
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public FieldDef save(FieldDef fieldDef) {
        FieldDefEntity entity = converter.toEntity(fieldDef);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insertFieldDef(entity);
        fieldDef.setId(entity.getId());
        return fieldDef;
    }

    @Override
    public FieldDef findById(Long id) {
        FieldDefEntity entity = mapper.selectByIdValue(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<FieldDef> findByVersionId(Long versionId) {
        List<FieldDefEntity> entities = mapper.selectByVersionId(versionId);
        return converter.toDomainList(entities);
    }

    @Override
    public void update(FieldDef fieldDef) {
        FieldDefEntity entity = converter.toEntity(fieldDef);
        mapper.updateFieldDef(entity);
    }

    @Override
    public boolean existsByFieldCode(String fieldCode) {
        return mapper.countByFieldCode(fieldCode) > 0;
    }

    @Override
    public boolean existsByFieldPath(String fieldPath) {
        return mapper.countByFieldPath(fieldPath) > 0;
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        mapper.physicalDeleteByVersionId(versionId);
    }
}
