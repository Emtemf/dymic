package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.FieldDefEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字段定义 Mapper
 */
@Mapper
public interface FieldDefMapper extends BaseMapper<FieldDefEntity> {
}