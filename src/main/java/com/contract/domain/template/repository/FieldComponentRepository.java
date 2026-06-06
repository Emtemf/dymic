package com.contract.domain.template.repository;

import com.contract.domain.template.FieldComponent;
import java.util.List;

/**
 * 字段组件绑定仓储接口
 */
public interface FieldComponentRepository {
    FieldComponent save(FieldComponent component);
    FieldComponent findById(Long id);
    List<FieldComponent> findByVersionId(Long versionId);
    List<FieldComponent> findByFieldDefId(Long fieldDefId);
    List<FieldComponent> findByLayoutNodeId(Long layoutNodeId);
    void update(FieldComponent component);
    void deleteById(Long id);
}