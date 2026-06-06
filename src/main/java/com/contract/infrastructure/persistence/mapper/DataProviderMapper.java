package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.DataProviderEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据提供方Mapper
 */
@Mapper
public interface DataProviderMapper extends BaseMapper<DataProviderEntity> {
}