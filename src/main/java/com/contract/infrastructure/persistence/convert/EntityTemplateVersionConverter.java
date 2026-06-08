package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.TemplateVersion.VersionStatus;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TemplateVersion Entity <-> Domain 转换器
 * 使用 default 方法 + reconstitute 工厂方法处理私有构造器
 */
@Mapper(componentModel = "spring")
public interface EntityTemplateVersionConverter {

    /**
     * Entity -> Domain
     */
    default TemplateVersion toDomain(TemplateVersionEntity entity) {
        if (entity == null) {
            return null;
        }
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
     * Domain -> Entity
     */
    default TemplateVersionEntity toEntity(TemplateVersion version) {
        if (version == null) {
            return null;
        }
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
        if (version.getVersionStatus() != null) {
            entity.setVersionStatus(version.getVersionStatus().name());
        }
        return entity;
    }

    /**
     * Entity List -> Domain List
     */
    default List<TemplateVersion> toDomainList(List<TemplateVersionEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * LocalDateTime -> OffsetDateTime
     */
    default OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

    /**
     * OffsetDateTime -> LocalDateTime
     */
    default LocalDateTime toLocalDateTime(OffsetDateTime odt) {
        return odt != null ? odt.toLocalDateTime() : null;
    }
}
