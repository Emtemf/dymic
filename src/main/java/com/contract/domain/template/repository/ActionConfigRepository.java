package com.contract.domain.template.repository;

import com.contract.domain.template.ActionConfig;
import java.util.List;

/**
 * 动作配置仓储接口
 */
public interface ActionConfigRepository {
    ActionConfig save(ActionConfig actionConfig);
    ActionConfig findById(Long id);
    List<ActionConfig> findByTemplateVersionId(Long templateVersionId);
    ActionConfig update(ActionConfig actionConfig);
    void deleteById(Long id);
    List<ActionConfig> findAll();
    void deleteByTemplateVersionId(Long versionId);
}