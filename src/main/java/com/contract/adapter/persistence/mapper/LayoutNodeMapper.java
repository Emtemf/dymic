package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.LayoutNodeEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 布局节点 Mapper
 */
@Mapper
public interface LayoutNodeMapper extends BaseMapper<LayoutNodeEntity> {
}