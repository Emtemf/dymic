package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.types.VersionStatus;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TemplateVersion Entity <-> Domain 转换器
 * 使用 default 方法 + reconstitute 工厂方法处理私有构造器
 */
@Mapper(componentModel = "spring")
public interface EntityTemplateVersionConverter {
    default TemplateVersion toDomain(TemplateVersionEntity entity) {
        if (entity == null) {
            return null;
        }
        VersionStatus status = entity.getVersionStatus() != null
            ? VersionStatus.valueOf(entity.getVersionStatus()) : null;
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

    default TemplateVersionEntity toEntity(TemplateVersion version) {
        if (version == null) {
            return null;
        }
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

    default List<TemplateVersion> toDomainList(List<TemplateVersionEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    default LocalDateTime map(OffsetDateTime value) {
        return value != null ? value.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
