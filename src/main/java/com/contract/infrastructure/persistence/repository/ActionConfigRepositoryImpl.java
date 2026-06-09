package com.contract.infrastructure.persistence.repository;

import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.repository.ActionConfigRepository;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.convert.EntityActionConfigConverter;
import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import com.contract.infrastructure.persistence.mapper.ActionConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动作配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ActionConfigRepositoryImpl implements ActionConfigRepository {

    private final ActionConfigMapper mapper;
    private final EntityActionConfigConverter converter;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public ActionConfig save(ActionConfig actionConfig) {
        ActionConfigEntity entity = converter.toEntity(actionConfig);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        mapper.insertActionConfig(entity);
        actionConfig.setId(entity.getId());
        return actionConfig;
    }

    @Override
    public ActionConfig findById(Long id) {
        ActionConfigEntity entity = mapper.selectByIdValue(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<ActionConfig> findByTemplateVersionId(Long templateVersionId) {
        List<ActionConfigEntity> entities = mapper.selectByTemplateVersionId(templateVersionId);
        return converter.toDomainList(entities);
    }

    @Override
    public ActionConfig update(ActionConfig actionConfig) {
        ActionConfigEntity entity = converter.toEntity(actionConfig);
        mapper.updateActionConfig(entity);
        return actionConfig;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteByIdValue(id);
    }

    @Override
    public List<ActionConfig> findAll() {
        return converter.toDomainList(mapper.selectAllActionConfigs());
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        mapper.physicalDeleteByVersionId(versionId);
    }
}
