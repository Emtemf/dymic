package com.contract.domain.template.repository;

import com.contract.domain.template.Template;

import java.util.List;

/**
 * 模板仓储接口
 * 遵循依赖倒置原则，定义在领域层
 */
public interface TemplateRepository {

    /**
     * 保存模板
     *
     * @param template 模板领域对象
     * @return 保存后的模板对象
     */
    Template save(Template template);

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板对象，不存在则返回null
     */
    Template findById(Long id);

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板对象，不存在则返回null
     */
    Template findByTemplateCode(String templateCode);

    /**
     * 查询所有模板
     *
     * @return 模板列表
     */
    List<Template> findAll();

    /**
     * 更新模板
     *
     * @param template 模板领域对象
     */
    void update(Template template);

    /**
     * 检查模板编码是否已存在
     *
     * @param templateCode 模板编码
     * @return true-存在, false-不存在
     */
    boolean existsByTemplateCode(String templateCode);
}
