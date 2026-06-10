package com.contract.infrastructure.persistence.repository;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateVersionRepository;
import com.contract.domain.template.types.VersionStatus;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import com.contract.infrastructure.persistence.mapper.TemplateVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public TemplateVersion save(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setIsDeleted(0);
        versionMapper.insertTemplateVersion(entity);
        return toDomain(entity);
    }

    @Override
    public TemplateVersion findById(Long id) {
        TemplateVersionEntity entity = versionMapper.selectByIdValue(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public TemplateVersion findByTemplateIdAndVersionNo(Long templateId, Integer versionNo) {
        TemplateVersionEntity entity = versionMapper.selectByTemplateIdAndVersionNo(templateId, versionNo);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public TemplateVersion findCurrentVersion(Long templateId) {
        TemplateVersionEntity entity = versionMapper.findCurrentVersion(templateId);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public List<TemplateVersion> findByTemplateId(Long templateId) {
        List<TemplateVersionEntity> entities = versionMapper.selectByTemplateId(templateId);
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void update(TemplateVersion version) {
        TemplateVersionEntity entity = toEntity(version);
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        versionMapper.updateTemplateVersion(entity);
    }

    @Override
    public boolean existsByTemplateIdAndVersionNo(Long templateId, Integer versionNo) {
        return versionMapper.countByTemplateIdAndVersionNo(templateId, versionNo) > 0;
    }

    private TemplateVersion toDomain(TemplateVersionEntity entity) {
        VersionStatus status = entity.getVersionStatus() != null ? VersionStatus.valueOf(entity.getVersionStatus()) : null;
        AuditInfo auditInfo = AuditInfo.of(
            entity.getCreatedBy(),
            entity.getCreatedName(),
            entity.getCreatedAt(),
            entity.getUpdatedBy(),
            entity.getUpdatedName(),
            entity.getUpdatedAt()
        );
        return TemplateVersion.reconstitute(
            entity.getId(),
            entity.getTemplateId(),
            entity.getVersionNo(),
            entity.getVersionName(),
            status,
            entity.getPublishTime(),
            entity.getPublishBy(),
            entity.getSchemaHash(),
            entity.getRemark(),
            auditInfo
        );
    }

    private TemplateVersionEntity toEntity(TemplateVersion version) {
        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setId(version.getId());
        entity.setTemplateId(version.getTemplateId());
        entity.setVersionNo(version.getVersionNo());
        entity.setVersionName(version.getVersionName());
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
        if (version.getVersionStatus() != null) {
            entity.setVersionStatus(version.getVersionStatus().name());
        }
        return entity;
    }

    private static LocalDateTime toUtcLocalDateTime(OffsetDateTime odt) {
        return odt != null ? odt.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
