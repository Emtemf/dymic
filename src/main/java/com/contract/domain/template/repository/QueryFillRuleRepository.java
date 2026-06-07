package com.contract.domain.template.repository;

import com.contract.domain.template.QueryFillRule;
import java.util.List;

/**
 * 查询回填规则仓储接口
 */
public interface QueryFillRuleRepository {
    QueryFillRule save(QueryFillRule rule);
    List<QueryFillRule> findByQueryConfigId(Long queryConfigId);
    void deleteByQueryConfigId(Long queryConfigId);
}
