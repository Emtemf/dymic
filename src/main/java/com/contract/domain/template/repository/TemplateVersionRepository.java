package com.contract.domain.template.repository;

import com.contract.domain.template.TemplateVersion;

import java.util.List;

/**
 * 模板版本仓储接口
 * 遵循依赖倒置原则，定义在领域层
 */
public interface TemplateVersionRepository {

    /**
     * 保存版本
     *
     * @param version 版本领域对象
     * @return 保存后的版本对象
     */
    TemplateVersion save(TemplateVersion version);

    /**
     * 根据ID查询版本
     *
     * @param id 版本ID
     * @return 版本对象，不存在则返回null
     */
    TemplateVersion findById(Long id);

    /**
     * 根据模板ID和版本号查询版本
     *
     * @param templateId 模板ID
     * @param versionNo 版本号
     * @return 版本对象，不存在则返回null
     */
    TemplateVersion findByTemplateIdAndVersionNo(Long templateId, Integer versionNo);

    /**
     * 查询模板的当前发布版本
     *
     * @param templateId 模板ID
     * @return 当前发布版本，不存在则返回null
     */
    TemplateVersion findCurrentVersion(Long templateId);

    /**
     * 查询模板的所有版本
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    List<TemplateVersion> findByTemplateId(Long templateId);

    /**
     * 更新版本
     *
     * @param version 版本领域对象
     */
    void update(TemplateVersion version);

    /**
     * 检查版本号是否已存在
     *
     * @param templateId 模板ID
     * @param versionNo 版本号
     * @return true-存在, false-不存在
     */
    boolean existsByTemplateIdAndVersionNo(Long templateId, Integer versionNo);
}
