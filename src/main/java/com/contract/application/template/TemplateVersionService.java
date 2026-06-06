package com.contract.application.template;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contract.adapter.persistence.entity.TemplateVersionEntity;
import com.contract.adapter.persistence.mapper.TemplateVersionMapper;
import com.contract.common.exception.BizException;
import com.contract.domain.template.TemplateVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 模板版本服务层
 * 负责模板版本的创建、发布、查询等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateVersionService {

    private final TemplateVersionMapper versionMapper;
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
        LambdaQueryWrapper<TemplateVersionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TemplateVersionEntity::getTemplateId, templateId)
               .eq(TemplateVersionEntity::getVersionNo, versionNo);
        if (versionMapper.selectCount(wrapper) > 0) {
            throw new BizException("版本号已存在：" + versionNo);
        }

        // 使用领域模型创建草稿版本
        TemplateVersion version = TemplateVersion.createDraft(templateId, versionNo, versionName);

        // 转换为实体并保存
        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setTemplateId(version.getTemplateId());
        entity.setVersionNo(version.getVersionNo());
        entity.setVersionName(version.getVersionName());
        entity.setVersionStatus(version.getVersionStatus());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        versionMapper.insert(entity);
        version.setId(entity.getId());

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
        TemplateVersionEntity entity = versionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("版本不存在：" + id);
        }
        return toDomain(entity);
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

        TemplateVersionEntity entity = versionMapper.selectById(versionId);
        if (entity == null) {
            throw new BizException("版本不存在：" + versionId);
        }

        // 检查是否为草稿状态
        if (!"DRAFT".equals(entity.getVersionStatus())) {
            throw new BizException("只有草稿状态才能发布");
        }

        // 更新版本状态为已发布
        entity.setVersionStatus("PUBLISHED");
        entity.setPublishTime(LocalDateTime.now());
        entity.setPublishBy(publishBy);
        entity.setUpdatedAt(LocalDateTime.now());
        versionMapper.updateById(entity);

        // 更新模板的当前版本ID
        templateService.updateCurrentVersion(entity.getTemplateId(), versionId);

        log.info("Version published successfully: {}", versionId);
    }

    /**
     * 查找模板的当前发布版本
     *
     * @param templateId 模板ID
     * @return 当前发布版本，如果没有则返回null
     */
    public TemplateVersion findCurrentVersion(Long templateId) {
        TemplateVersionEntity entity = versionMapper.findCurrentVersion(templateId);
        if (entity == null) {
            return null;
        }
        return toDomain(entity);
    }

    /**
     * 实体转领域模型
     *
     * @param entity 实体对象
     * @return 领域模型对象
     */
    private TemplateVersion toDomain(TemplateVersionEntity entity) {
        TemplateVersion version = new TemplateVersion();
        version.setId(entity.getId());
        version.setTemplateId(entity.getTemplateId());
        version.setVersionNo(entity.getVersionNo());
        version.setVersionName(entity.getVersionName());
        version.setVersionStatus(entity.getVersionStatus());
        version.setPublishTime(entity.getPublishTime());
        version.setPublishBy(entity.getPublishBy());
        version.setSchemaHash(entity.getSchemaHash());
        version.setRemark(entity.getRemark());
        version.setCreatedBy(entity.getCreatedBy());
        version.setCreatedName(entity.getCreatedName());
        version.setCreatedAt(entity.getCreatedAt());
        version.setUpdatedBy(entity.getUpdatedBy());
        version.setUpdatedName(entity.getUpdatedName());
        version.setUpdatedAt(entity.getUpdatedAt());
        return version;
    }
}