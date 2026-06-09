package com.contract.domain.template.service;

import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateRepository;
import com.contract.domain.template.repository.TemplateVersionRepository;
import com.contract.domain.template.types.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateDomainService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;

    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        if (templateRepository.existsByTemplateCode(templateCode)) {
            throw new BizException("模板编码已存在：" + templateCode);
        }
        Template template = Template.create(
            new TemplateCode(templateCode),
            new TemplateName(templateName),
            templateDesc != null ? new TemplateDesc(templateDesc) : null,
            bizType != null ? new BizType(bizType) : null
        );
        template = templateRepository.save(template);

        TemplateVersion version = TemplateVersion.createDraft(template.getIdValue(), 1, "初始版本");
        versionRepository.save(version);
        return template;
    }

    public Template getById(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        return template;
    }

    public Template getByCode(String code) {
        Template template = templateRepository.findByTemplateCode(code);
        if (template == null) {
            throw new BizException("模板不存在：" + code);
        }
        return template;
    }

    public void disable(Long id) {
        Template template = getById(id);
        template.disable();
        templateRepository.update(template);
    }

    public void enable(Long id) {
        Template template = getById(id);
        template.enable();
        templateRepository.update(template);
    }

    public void updateCurrentVersion(Long templateId, Long versionId) {
        Template template = getById(templateId);
        template.setCurrentVersion(versionId);
        templateRepository.update(template);
    }

    public List<Template> listAll() {
        return templateRepository.findAll();
    }
}
