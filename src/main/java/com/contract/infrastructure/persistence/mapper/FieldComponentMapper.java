package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段组件绑定Mapper
 */
@Mapper
public interface FieldComponentMapper extends BaseMapper<FieldComponentEntity> {
}