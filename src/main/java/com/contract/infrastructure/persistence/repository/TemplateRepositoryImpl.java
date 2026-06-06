package com.contract.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateEntity;
import com.contract.adapter.persistence.mapper.TemplateMapper;
import com.contract.domain.template.Template;
import com.contract.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
        template.setId(entity.getId());
        return template;
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
        Template template = new Template();
        template.setId(entity.getId());
        template.setTemplateCode(entity.getTemplateCode());
        template.setTemplateName(entity.getTemplateName());
        template.setTemplateDesc(entity.getTemplateDesc());
        template.setBizType(entity.getBizType());
        template.setStatus(entity.getStatus());
        template.setCurrentVersionId(entity.getCurrentVersionId());
        template.setCreatedBy(entity.getCreatedBy());
        template.setCreatedName(entity.getCreatedName());
        template.setCreatedAt(entity.getCreatedAt());
        template.setUpdatedBy(entity.getUpdatedBy());
        template.setUpdatedName(entity.getUpdatedName());
        template.setUpdatedAt(entity.getUpdatedAt());
        return template;
    }

    /**
     * Domain 转 Entity
     */
    private TemplateEntity toEntity(Template template) {
        TemplateEntity entity = new TemplateEntity();
        entity.setId(template.getId());
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setTemplateDesc(template.getTemplateDesc());
        entity.setBizType(template.getBizType());
        entity.setStatus(template.getStatus());
        entity.setCurrentVersionId(template.getCurrentVersionId());
        entity.setCreatedBy(template.getCreatedBy());
        entity.setCreatedName(template.getCreatedName());
        entity.setCreatedAt(template.getCreatedAt());
        entity.setUpdatedBy(template.getUpdatedBy());
        entity.setUpdatedName(template.getUpdatedName());
        entity.setUpdatedAt(template.getUpdatedAt());
        return entity;
    }
}
