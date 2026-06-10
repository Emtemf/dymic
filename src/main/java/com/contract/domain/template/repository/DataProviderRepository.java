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

    /**
     * 按类型查询DataProvider列表
     *
     * @param providerType 类型：STATIC/DICT/HTTP/PLATFORM/INTERNAL
     * @return 该类型的DataProvider列表
     */
    List<DataProvider> findByType(String providerType);
    List<DataProvider> findByCategory(String category);

    /**
     * 查询或创建字典DataProvider
     *
     * @param dictType 字典类型编码
     * @return 字典DataProvider
     */
    DataProvider findByDictType(String dictType);
}