package com.contract.infrastructure.persistence.convert;

import com.contract.domain.template.Template;
import com.contract.domain.template.Template.TemplateStatus;
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
        TemplateStatus status = entity.getStatus() != null
            ? TemplateStatus.valueOf(entity.getStatus()) : null;
        return Template.reconstitute(
            entity.getId(),
            entity.getTemplateCode(),
            entity.getTemplateName(),
            entity.getTemplateDesc(),
            entity.getBizType(),
            status,
            entity.getCurrentVersionId(),
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
    default TemplateEntity toEntity(Template template) {
        if (template == null) {
            return null;
        }
        TemplateEntity entity = new TemplateEntity();
        entity.setId(template.getId());
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setTemplateDesc(template.getTemplateDesc());
        entity.setBizType(template.getBizType());
        entity.setCurrentVersionId(template.getCurrentVersionId());
        entity.setCreatedBy(template.getCreatedBy());
        entity.setCreatedName(template.getCreatedName());
        entity.setCreatedAt(toLocalDateTime(template.getCreatedAt()));
        entity.setUpdatedBy(template.getUpdatedBy());
        entity.setUpdatedName(template.getUpdatedName());
        entity.setUpdatedAt(toLocalDateTime(template.getUpdatedAt()));
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
