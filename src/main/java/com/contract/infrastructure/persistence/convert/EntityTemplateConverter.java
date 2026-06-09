package com.contract.infrastructure.persistence.convert;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.Template;
import com.contract.domain.template.types.TemplateStatus;
import com.contract.domain.template.types.*;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template Entity <-> Domain 转换器
 * 使用 default 方法 + reconstitute 工厂方法处理私有构造器
 */
@Mapper(componentModel = "spring")
public interface EntityTemplateConverter {

    /**
     * Entity -> Domain
     */
    default Template toDomain(TemplateEntity entity) {
        if (entity == null) {
            return null;
        }

        // 转换值对象
        TemplateId id = entity.getId() != null ? new TemplateId(entity.getId()) : null;
        TemplateCode templateCode = entity.getTemplateCode() != null ? new TemplateCode(entity.getTemplateCode()) : null;
        TemplateName templateName = entity.getTemplateName() != null ? new TemplateName(entity.getTemplateName()) : null;
        TemplateDesc templateDesc = entity.getTemplateDesc() != null ? new TemplateDesc(entity.getTemplateDesc()) : null;
        BizType bizType = entity.getBizType() != null ? new BizType(entity.getBizType()) : null;
        TemplateStatus status = entity.getStatus() != null
            ? TemplateStatus.valueOf(entity.getStatus()) : null;

        // 转换审计信息
        AuditInfo auditInfo = AuditInfo.of(
            entity.getCreatedBy(),
            entity.getCreatedName(),
            toOffsetDateTime(entity.getCreatedAt()),
            entity.getUpdatedBy(),
            entity.getUpdatedName(),
            toOffsetDateTime(entity.getUpdatedAt())
        );

        return Template.reconstitute(
            id,
            templateCode,
            templateName,
            templateDesc,
            bizType,
            status,
            entity.getCurrentVersionId(),
            auditInfo
        );
    }

    /**
     * Domain -> Entity
     */
    default TemplateEntity toEntity(Template template) {
        if (template == null) {
            return null;
        }
        TemplateEntity entity = new TemplateEntity();

        // 处理值对象
        if (template.getId() != null) {
            entity.setId(template.getId().getValue());
        }
        if (template.getTemplateCode() != null) {
            entity.setTemplateCode(template.getTemplateCode().getValue());
        }
        if (template.getTemplateName() != null) {
            entity.setTemplateName(template.getTemplateName().getValue());
        }
        if (template.getTemplateDesc() != null) {
            entity.setTemplateDesc(template.getTemplateDesc().getValue());
        }
        if (template.getBizType() != null) {
            entity.setBizType(template.getBizType().getValue());
        }

        entity.setCurrentVersionId(template.getCurrentVersionId());

        // 处理审计信息
        if (template.getAuditInfo() != null) {
            AuditInfo auditInfo = template.getAuditInfo();
            entity.setCreatedBy(auditInfo.getCreatedBy());
            entity.setCreatedName(auditInfo.getCreatedName());
            entity.setCreatedAt(toLocalDateTime(auditInfo.getCreatedAt()));
            entity.setUpdatedBy(auditInfo.getUpdatedBy());
            entity.setUpdatedName(auditInfo.getUpdatedName());
            entity.setUpdatedAt(toLocalDateTime(auditInfo.getUpdatedAt()));
        }

        // Status: Enum -> String
        if (template.getStatus() != null) {
            entity.setStatus(template.getStatus().name());
        }
        return entity;
    }

    /**
     * Entity List -> Domain List
     */
    default List<Template> toDomainList(List<TemplateEntity> entities) {
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
