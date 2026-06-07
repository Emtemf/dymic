package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import com.contract.infrastructure.persistence.mapper.ActionConfigMapper;
import com.contract.domain.template.ActionConfig;
import com.contract.domain.template.repository.ActionConfigRepository;
import com.contract.infrastructure.persistence.convert.EntityActionConfigConverter;
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

    @Override
    public ActionConfig save(ActionConfig actionConfig) {
        ActionConfigEntity entity = converter.toEntity(actionConfig);
        mapper.insert(entity);
        actionConfig.setId(entity.getId());
        return actionConfig;
    }

    @Override
    public ActionConfig findById(Long id) {
        ActionConfigEntity entity = mapper.selectById(id);
        return entity != null ? converter.toDomain(entity) : null;
    }

    @Override
    public List<ActionConfig> findByTemplateVersionId(Long templateVersionId) {
        LambdaQueryWrapper<ActionConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActionConfigEntity::getTemplateVersionId, templateVersionId);
        wrapper.orderByAsc(ActionConfigEntity::getSortNo);
        List<ActionConfigEntity> entities = mapper.selectList(wrapper);
        return converter.toDomainList(entities);
    }

    @Override
    public ActionConfig update(ActionConfig actionConfig) {
        ActionConfigEntity entity = converter.toEntity(actionConfig);
        mapper.updateById(entity);
        return actionConfig;
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public List<ActionConfig> findAll() {
        List<ActionConfigEntity> entities = mapper.selectList(null);
        return converter.toDomainList(entities);
    }

    @Override
    public void deleteByTemplateVersionId(Long versionId) {
        LambdaQueryWrapper<ActionConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActionConfigEntity::getTemplateVersionId, versionId);
        mapper.delete(wrapper);
    }
}