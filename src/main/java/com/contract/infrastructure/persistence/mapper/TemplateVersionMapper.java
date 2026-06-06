package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 模板版本Mapper
 */
@Mapper
public interface TemplateVersionMapper extends BaseMapper<TemplateVersionEntity> {

    /**
     * 查找当前发布的版本（最新的已发布版本）
     */
    @Select("SELECT * FROM t_ui_template_version WHERE template_id = #{templateId} AND version_status = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1")
    TemplateVersionEntity findCurrentVersion(@Param("templateId") Long templateId);
}