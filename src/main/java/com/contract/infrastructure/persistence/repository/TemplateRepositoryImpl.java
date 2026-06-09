package com.contract.infrastructure.persistence.repository;

import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.Template;
import com.contract.domain.template.repository.TemplateRepository;
import com.contract.domain.template.types.BizType;
import com.contract.domain.template.types.TemplateCode;
import com.contract.domain.template.types.TemplateDesc;
import com.contract.domain.template.types.TemplateId;
import com.contract.domain.template.types.TemplateName;
import com.contract.domain.template.types.TemplateStatus;
import com.contract.infrastructure.id.SnowflakeIdGenerator;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import com.contract.infrastructure.persistence.mapper.TemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板仓储实现
 * 负责 Entity <-> Domain 转换
 */
@Repository
@RequiredArgsConstructor
public class TemplateRepositoryImpl implements TemplateRepository {

    private final TemplateMapper templateMapper;
    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Template save(Template template) {
        TemplateEntity entity = toEntity(template);
        if (entity.getId() == null) {
            entity.setId(idGenerator.nextId());
        }
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setIsDeleted(0);
        templateMapper.insertTemplate(entity);
        return toDomain(entity);
    }

    @Override
    public Template findById(Long id) {
        TemplateEntity entity = templateMapper.selectByIdValue(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public Template findByTemplateCode(String templateCode) {
        TemplateEntity entity = templateMapper.selectByTemplateCode(templateCode);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public List<Template> findAll() {
        List<TemplateEntity> entities = templateMapper.selectAllTemplates();
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void update(Template template) {
        TemplateEntity entity = toEntity(template);
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateTemplate(entity);
    }

    @Override
    public boolean existsByTemplateCode(String templateCode) {
        return templateMapper.countByTemplateCode(templateCode) > 0;
    }

    private Template toDomain(TemplateEntity entity) {
        TemplateId id = entity.getId() != null ? new TemplateId(entity.getId()) : null;
        TemplateCode templateCode = entity.getTemplateCode() != null ? new TemplateCode(entity.getTemplateCode()) : null;
        TemplateName templateName = entity.getTemplateName() != null ? new TemplateName(entity.getTemplateName()) : null;
        TemplateDesc templateDesc = entity.getTemplateDesc() != null ? new TemplateDesc(entity.getTemplateDesc()) : null;
        BizType bizType = entity.getBizType() != null ? new BizType(entity.getBizType()) : null;
        TemplateStatus status = entity.getStatus() != null ? TemplateStatus.valueOf(entity.getStatus()) : null;

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

    private TemplateEntity toEntity(Template template) {
        TemplateEntity entity = new TemplateEntity();
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
        if (template.getAuditInfo() != null) {
            AuditInfo auditInfo = template.getAuditInfo();
            entity.setCreatedBy(auditInfo.getCreatedBy());
            entity.setCreatedName(auditInfo.getCreatedName());
            entity.setCreatedAt(toLocalDateTime(auditInfo.getCreatedAt()));
            entity.setUpdatedBy(auditInfo.getUpdatedBy());
            entity.setUpdatedName(auditInfo.getUpdatedName());
            entity.setUpdatedAt(toLocalDateTime(auditInfo.getUpdatedAt()));
        }
        if (template.getStatus() != null) {
            entity.setStatus(template.getStatus().name());
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
