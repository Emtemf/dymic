package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.types.VersionStatus;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * 模板版本转换器
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateVersionConverter {

    TemplateVersionConverter INSTANCE = org.mapstruct.factory.Mappers.getMapper(TemplateVersionConverter.class);

    // ===== Domain → Entity =====

    @Mapping(target = "id", source = "id")
    @Mapping(target = "publishTime", expression = "java(toLocalDateTime(domain.getPublishTime()))")
    @Mapping(target = "versionStatus", expression = "java(domain.getVersionStatus().name())")
    @Mapping(target = "createdBy", expression = "java(domain.getAuditInfo().getCreatedBy())")
    @Mapping(target = "createdName", expression = "java(domain.getAuditInfo().getCreatedName())")
    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(domain.getAuditInfo().getCreatedAt()))")
    @Mapping(target = "updatedBy", expression = "java(domain.getAuditInfo().getUpdatedBy())")
    @Mapping(target = "updatedName", expression = "java(domain.getAuditInfo().getUpdatedName())")
    @Mapping(target = "updatedAt", expression = "java(toLocalDateTime(domain.getAuditInfo().getUpdatedAt()))")
    TemplateVersionEntity toEntity(TemplateVersion domain);

    // ===== Entity → Domain =====

    default TemplateVersion toDomain(TemplateVersionEntity entity) {
        if (entity == null) return null;

        return TemplateVersion.reconstitute(
            entity.getId(),
            entity.getTemplateId(),
            entity.getVersionNo(),
            entity.getVersionName(),
            VersionStatus.valueOf(entity.getVersionStatus()),
            toOffsetDateTime(entity.getPublishTime()),
            entity.getPublishBy(),
            entity.getSchemaHash(),
            entity.getRemark(),
            AuditInfo.of(
                entity.getCreatedBy(),
                entity.getCreatedName(),
                toOffsetDateTime(entity.getCreatedAt()),
                entity.getUpdatedBy(),
                entity.getUpdatedName(),
                toOffsetDateTime(entity.getUpdatedAt())
            )
        );
    }

    // ===== 日期时间转换 =====

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
