package com.contract.application.template;

import com.contract.common.exception.BizException;
import com.contract.domain.template.TemplateVersion;
import com.contract.domain.template.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板版本服务层
 * 负责模板版本的创建、发布、查询等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateVersionService {

    private final TemplateVersionRepository versionRepository;
    private final TemplateService templateService;

    /**
     * 创建草稿版本
     *
     * @param templateId 模板ID
     * @param versionNo 版本号
     * @param versionName 版本名称
     * @return 创建的版本对象
     */
    @Transactional
    public TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        log.info("Creating draft version for template: {}, versionNo: {}", templateId, versionNo);

        // 检查模板是否存在
        templateService.getById(templateId);

        // 检查版本号是否已存在
        if (versionRepository.existsByTemplateIdAndVersionNo(templateId, versionNo)) {
            throw new BizException("版本号已存在：" + versionNo);
        }

        // 使用领域模型创建草稿版本
        TemplateVersion version = TemplateVersion.createDraft(templateId, versionNo, versionName);
        version = versionRepository.save(version);

        log.info("Draft version created successfully: {}", version.getId());
        return version;
    }

    /**
     * 按ID查询版本
     *
     * @param id 版本ID
     * @return 版本对象
     */
    public TemplateVersion getById(Long id) {
        TemplateVersion version = versionRepository.findById(id);
        if (version == null) {
            throw new BizException("版本不存在：" + id);
        }
        return version;
    }

    /**
     * 发布版本
     * 只有草稿状态才能发布
     *
     * @param versionId 版本ID
     * @param publishBy 发布人ID
     */
    @Transactional
    public void publish(Long versionId, Long publishBy) {
        log.info("Publishing version: {}, by user: {}", versionId, publishBy);

        TemplateVersion version = versionRepository.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在：" + versionId);
        }

        // 使用领域模型发布版本（会校验状态）
        version.publish(publishBy);

        // 更新版本
        versionRepository.update(version);

        // 更新模板的当前版本ID
        templateService.updateCurrentVersion(version.getTemplateId(), versionId);

        log.info("Version published successfully: {}", versionId);
    }

    /**
     * 查找模板的当前发布版本
     *
     * @param templateId 模板ID
     * @return 当前发布版本，如果没有则返回null
     */
    public TemplateVersion findCurrentVersion(Long templateId) {
        return versionRepository.findCurrentVersion(templateId);
    }

    /**
     * 查询模板的所有版本列表
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    public List<TemplateVersion> findByTemplateId(Long templateId) {
        return versionRepository.findByTemplateId(templateId);
    }
}