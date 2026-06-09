package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据提供方Mapper
 */
@Mapper
public interface DataProviderMapper {
    int insertDataProvider(DataProviderEntity entity);

    DataProviderEntity selectByIdValue(@Param("id") Long id);

    DataProviderEntity selectByProviderCode(@Param("providerCode") String providerCode);

    long countByProviderCode(@Param("providerCode") String providerCode);

    int updateDataProvider(DataProviderEntity entity);

    List<DataProviderEntity> selectAllDataProviders();

    List<DataProviderEntity> selectByIds(@Param("ids") List<Long> ids);

    List<DataProviderEntity> selectByType(@Param("providerType") String providerType);

    List<DataProviderEntity> selectByCategory(@Param("category") String category);

    DataProviderEntity selectDictByType(@Param("dictType") String dictType);
}
