package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.TemplateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模板Mapper
 */
@Mapper
public interface TemplateMapper extends BaseMapper<TemplateEntity> {
}