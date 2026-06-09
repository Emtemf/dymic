package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.Template;
import com.contract.domain.template.types.TemplateStatus;
import com.contract.domain.template.repository.TemplateRepository;
import com.contract.domain.template.types.*;
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

    @Override
    public Template save(Template template) {
        TemplateEntity entity = toEntity(template);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.insert(entity);
        // 更新Domain的ID（这里需要特殊处理，因为DDD不应该有setter）
        // 实际项目中，应该使用reconstitute重新构建一个带ID的实例
        return toDomain(entity);
    }

    @Override
    public Template findById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public Template findByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        TemplateEntity entity = templateMapper.selectOne(wrapper);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public List<Template> findAll() {
        List<TemplateEntity> entities = templateMapper.selectList(null);
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void update(Template template) {
        TemplateEntity entity = toEntity(template);
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    @Override
    public boolean existsByTemplateCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        return templateMapper.selectCount(wrapper) > 0;
    }

    /**
     * Entity 转 Domain
     */
    private Template toDomain(TemplateEntity entity) {
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
     * Domain 转 Entity
     */
    private TemplateEntity toEntity(Template template) {
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

    private static OffsetDateTime toOffsetDateTime(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
    }

    private static LocalDateTime toLocalDateTime(OffsetDateTime odt) {
        return odt != null ? odt.toLocalDateTime() : null;
    }
}
