package com.contract.domain.template.repository;

import com.contract.domain.template.QueryConfig;
import java.util.List;

/**
 * 查询配置仓储接口
 */
public interface QueryConfigRepository {
    QueryConfig save(QueryConfig config);
    QueryConfig findById(Long id);
    List<QueryConfig> findByTemplateVersionId(Long versionId);
    void update(QueryConfig config);
    void deleteById(Long id);
}
