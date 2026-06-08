package com.contract.application.template;

import com.contract.domain.template.Template;
import com.contract.domain.template.service.TemplateDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateDomainService templateDomainService;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        return templateDomainService.createTemplate(templateCode, templateName, templateDesc, bizType);
    }

    public Template getById(Long id) {
        return templateDomainService.getById(id);
    }

    public Template getByCode(String templateCode) {
        return templateDomainService.getByCode(templateCode);
    }

    @Transactional
    public void disable(Long id) {
        templateDomainService.disable(id);
    }

    @Transactional
    public void enable(Long id) {
        templateDomainService.enable(id);
    }

    @Transactional
    public void updateCurrentVersion(Long templateId, Long versionId) {
        templateDomainService.updateCurrentVersion(templateId, versionId);
    }

    public List<Template> listAll() {
        return templateDomainService.listAll();
    }
}