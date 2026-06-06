package com.contract.domain.template.repository;

import com.contract.domain.template.DataProvider;
import java.util.List;

/**
 * 数据提供方仓储接口
 */
public interface DataProviderRepository {
    DataProvider save(DataProvider provider);
    DataProvider findById(Long id);
    DataProvider findByProviderCode(String providerCode);
    boolean existsByProviderCode(String providerCode);
    void update(DataProvider provider);
    List<DataProvider> findAll();
    List<DataProvider> findByIds(List<Long> ids);
}