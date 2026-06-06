package com.contract.application.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateEntity;
import com.contract.adapter.persistence.mapper.TemplateMapper;
import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateMapper templateMapper;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        // 检查编码是否已存在
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        if (templateMapper.selectCount(wrapper) > 0) {
            throw new BizException("模板编码已存在：" + templateCode);
        }

        Template template = Template.create(templateCode, templateName, templateDesc, bizType);

        TemplateEntity entity = new TemplateEntity();
        entity.setTemplateCode(template.getTemplateCode());
        entity.setTemplateName(template.getTemplateName());
        entity.setTemplateDesc(template.getTemplateDesc());
        entity.setBizType(template.getBizType());
        entity.setStatus(template.getStatus());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        templateMapper.insert(entity);
        template.setId(entity.getId());

        return template;
    }

    public Template getById(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        return toDomain(entity);
    }

    public Template getByCode(String templateCode) {
        LambdaQueryWrapper<TemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateEntity::getTemplateCode, templateCode);
        TemplateEntity entity = templateMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BizException("模板不存在：" + templateCode);
        }
        return toDomain(entity);
    }

    @Transactional
    public void disable(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        entity.setStatus("DISABLED");
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

    @Transactional
    public void enable(Long id) {
        TemplateEntity entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BizException("模板不存在：" + id);
        }
        entity.setStatus("ENABLED");
        entity.setUpdatedAt(LocalDateTime.now());
        templateMapper.updateById(entity);
    }

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
}