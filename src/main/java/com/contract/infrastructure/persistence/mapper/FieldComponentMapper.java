package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.FieldComponentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FieldComponentMapper extends BaseMapper<FieldComponentEntity> {
}