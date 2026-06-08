package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import com.contract.infrastructure.persistence.mapper.TemplateVersionMapper;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.TemplateVersion.VersionStatus;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
        VersionStatus status = entity.getVersionStatus() != null
            ? VersionStatus.valueOf(entity.getVersionStatus()) : null;
        return TemplateVersion.reconstitute(
            entity.getId(),
            entity.getTemplateId(),
            entity.getVersionNo(),
            entity.getVersionName(),
            status,
            toOffsetDateTime(entity.getPublishTime()),
            entity.getPublishBy(),
            entity.getSchemaHash(),
            entity.getRemark(),
            entity.getCreatedBy(),
            entity.getCreatedName(),
            toOffsetDateTime(entity.getCreatedAt()),
            entity.getUpdatedBy(),
            entity.getUpdatedName(),
            toOffsetDateTime(entity.getUpdatedAt())
        );
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
        entity.setPublishTime(toLocalDateTime(version.getPublishTime()));
        entity.setPublishBy(version.getPublishBy());
        entity.setSchemaHash(version.getSchemaHash());
        entity.setRemark(version.getRemark());
        entity.setCreatedBy(version.getCreatedBy());
        entity.setCreatedName(version.getCreatedName());
        entity.setCreatedAt(toLocalDateTime(version.getCreatedAt()));
        entity.setUpdatedBy(version.getUpdatedBy());
        entity.setUpdatedName(version.getUpdatedName());
        entity.setUpdatedAt(toLocalDateTime(version.getUpdatedAt()));
        // VersionStatus: Enum -> String
        if (version.getVersionStatus() != null) {
            entity.setVersionStatus(version.getVersionStatus().name());
        }
        return entity;
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime odt) {
        return odt != null ? odt.toLocalDateTime() : null;
    }
}
