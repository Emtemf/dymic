package com.contract.domain.template.repository;

import com.contract.domain.template.FieldDef;
import java.util.List;

/**
 * 字段定义仓储接口
 */
public interface FieldDefRepository {
    FieldDef save(FieldDef fieldDef);
    FieldDef findById(Long id);
    List<FieldDef> findByVersionId(Long versionId);
    List<FieldDef> findByLayoutNodeId(Long layoutNodeId);
    void update(FieldDef fieldDef);
    boolean existsByFieldCode(String fieldCode);
}