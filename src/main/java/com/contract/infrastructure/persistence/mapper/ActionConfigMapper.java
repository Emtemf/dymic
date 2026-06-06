package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动作配置Mapper
 */
@Mapper
public interface ActionConfigMapper extends BaseMapper<ActionConfigEntity> {
}