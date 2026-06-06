package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 布局节点Mapper
 */
@Mapper
public interface LayoutNodeMapper extends BaseMapper<LayoutNodeEntity> {
}