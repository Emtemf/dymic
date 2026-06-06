package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import com.contract.infrastructure.persistence.mapper.TemplateVersionMapper;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板版本仓储实现
 * 负责 Entity <-> Domain 转换
 */
@Repository
@RequiredArgsConstructor
public class TemplateVersionRepositoryImpl implements TemplateVersionRepository {

    private final TemplateVersionMapper versionMapper;

    @Override
    public TemplateVersion save(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        versionMapper.insert(entity);
        version.setId(entity.getId());
        return version;
    }

    @Override
    public TemplateVersion findById(Long id) {
        TemplateVersionEntity entity = versionMapper.selectById(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public TemplateVersion findByTemplateIdAndVersionNo(Long templateId, Integer versionNo) {
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .eq(TemplateVersionEntity::getVersionNo, versionNo);
        TemplateVersionEntity entity = versionMapper.selectOne(wrapper);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public TemplateVersion findCurrentVersion(Long templateId) {
        TemplateVersionEntity entity = versionMapper.findCurrentVersion(templateId);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public List<TemplateVersion> findByTemplateId(Long templateId) {
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .orderByDesc(TemplateVersionEntity::getVersionNo);
        List<TemplateVersionEntity> entities = versionMapper.selectList(wrapper);
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void update(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        entity.setUpdatedAt(LocalDateTime.now());
        versionMapper.updateById(entity);
    }

    @Override
    public boolean existsByTemplateIdAndVersionNo(Long templateId, Integer versionNo) {
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .eq(TemplateVersionEntity::getVersionNo, versionNo);
        return versionMapper.selectCount(wrapper) > 0;
    }

    /**
     * Entity 转 Domain
     */
    private TemplateVersion toDomain(TemplateVersionEntity entity) {
        TemplateVersion version = new TemplateVersion();
        version.setId(entity.getId());
        version.setTemplateId(entity.getTemplateId());
        version.setVersionNo(entity.getVersionNo());
        version.setVersionName(entity.getVersionName());
        version.setVersionStatus(entity.getVersionStatus());
        version.setPublishTime(entity.getPublishTime());
        version.setPublishBy(entity.getPublishBy());
        version.setSchemaHash(entity.getSchemaHash());
        version.setRemark(entity.getRemark());
        version.setCreatedBy(entity.getCreatedBy());
        version.setCreatedName(entity.getCreatedName());
        version.setCreatedAt(entity.getCreatedAt());
        version.setUpdatedBy(entity.getUpdatedBy());
        version.setUpdatedName(entity.getUpdatedName());
        version.setUpdatedAt(entity.getUpdatedAt());
        return version;
    }

    /**
     * Domain 转 Entity
     */
    private TemplateVersionEntity toEntity(TemplateVersion version) {
        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setId(version.getId());
        entity.setTemplateId(version.getTemplateId());
        entity.setVersionNo(version.getVersionNo());
        entity.setVersionName(version.getVersionName());
        entity.setVersionStatus(version.getVersionStatus());
        entity.setPublishTime(version.getPublishTime());
        entity.setPublishBy(version.getPublishBy());
        entity.setSchemaHash(version.getSchemaHash());
        entity.setRemark(version.getRemark());
        entity.setCreatedBy(version.getCreatedBy());
        entity.setCreatedName(version.getCreatedName());
        entity.setCreatedAt(version.getCreatedAt());
        entity.setUpdatedBy(version.getUpdatedBy());
        entity.setUpdatedName(version.getUpdatedName());
        entity.setUpdatedAt(version.getUpdatedAt());
        return entity;
    }
}
