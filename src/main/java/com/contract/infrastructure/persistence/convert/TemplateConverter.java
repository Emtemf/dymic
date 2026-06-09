package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.Template;
import com.contract.domain.template.types.*;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * 模板转换器
 *
 * 【职责】Domain ↔ Entity 转换
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TemplateConverter {

    TemplateConverter INSTANCE = org.mapstruct.factory.Mappers.getMapper(TemplateConverter.class);

    // ===== Domain → Entity =====

    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "templateCode", expression = "java(domain.getTemplateCode().getValue())")
    @Mapping(target = "templateName", expression = "java(domain.getTemplateName().getValue())")
    @Mapping(target = "templateDesc", expression = "java(domain.getTemplateDesc() != null ? domain.getTemplateDesc().getValue() : null)")
    @Mapping(target = "bizType", expression = "java(domain.getBizType() != null ? domain.getBizType().getValue() : null)")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    @Mapping(target = "createdBy", expression = "java(domain.getAuditInfo().getCreatedBy())")
    @Mapping(target = "createdName", expression = "java(domain.getAuditInfo().getCreatedName())")
    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(domain.getAuditInfo().getCreatedAt()))")
    @Mapping(target = "updatedBy", expression = "java(domain.getAuditInfo().getUpdatedBy())")
    @Mapping(target = "updatedName", expression = "java(domain.getAuditInfo().getUpdatedName())")
    @Mapping(target = "updatedAt", expression = "java(toLocalDateTime(domain.getAuditInfo().getUpdatedAt()))")
    TemplateEntity toEntity(Template domain);

    // ===== Entity → Domain =====

    default Template toDomain(TemplateEntity entity) {
        if (entity == null) return null;

        return Template.reconstitute(
            entity.getId() != null ? new TemplateId(entity.getId()) : null,
            new TemplateCode(entity.getTemplateCode()),
            new TemplateName(entity.getTemplateName()),
            entity.getTemplateDesc() != null ? new TemplateDesc(entity.getTemplateDesc()) : null,
            entity.getBizType() != null ? new BizType(entity.getBizType()) : null,
            TemplateStatus.valueOf(entity.getStatus()),
            entity.getCurrentVersionId(),
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
