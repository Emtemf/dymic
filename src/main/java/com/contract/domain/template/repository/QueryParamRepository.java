package com.contract.domain.template.repository;

import com.contract.domain.template.QueryParam;
import java.util.List;

/**
 * 查询参数仓储接口
 */
public interface QueryParamRepository {
    QueryParam save(QueryParam param);
    List<QueryParam> findByQueryConfigId(Long queryConfigId);
    void deleteByQueryConfigId(Long queryConfigId);
}
