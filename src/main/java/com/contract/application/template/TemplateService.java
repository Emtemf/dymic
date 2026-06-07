package com.contract.application.template;

import com.contract.common.exception.BizException;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateRepository;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;

    @Transactional
    public Template createTemplate(String templateCode, String templateName, String templateDesc, String bizType) {
        // 检查编码是否已存在
        if (templateRepository.existsByTemplateCode(templateCode)) {
            throw new BizException("模板编码已存在：" + templateCode);
        }

        Template template = Template.create(templateCode, templateName, templateDesc, bizType);
        template = templateRepository.save(template);

        // 自动创建初始版本
        TemplateVersion version = TemplateVersion.createDraft(template.getId(), 1, "初始版本");
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

    public Template getByCode(String templateCode) {
        Template template = templateRepository.findByTemplateCode(templateCode);
        if (template == null) {
            throw new BizException("模板不存在：" + templateCode);
        }
        return template;
    }

    @Transactional
    public void disable(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        template.disable();
        templateRepository.update(template);
    }

    @Transactional
    public void enable(Long id) {
        Template template = templateRepository.findById(id);
        if (template == null) {
            throw new BizException("模板不存在：" + id);
        }
        template.enable();
        templateRepository.update(template);
    }

    /**
     * 更新模板的当前版本ID
     *
     * @param templateId 模板ID
     * @param versionId 版本ID
     */
    @Transactional
    public void updateCurrentVersion(Long templateId, Long versionId) {
        Template template = templateRepository.findById(templateId);
        if (template == null) {
            throw new BizException("模板不存在：" + templateId);
        }
        template.setCurrentVersion(versionId);
        templateRepository.update(template);
    }

    /**
     * 查询所有模板列表
     *
     * @return 模板列表
     */
    public List<Template> listAll() {
        return templateRepository.findAll();
    }
}