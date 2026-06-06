package com.contract.adapter.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.adapter.persistence.entity.TemplateVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TemplateVersionMapper extends BaseMapper<TemplateVersionEntity> {

    @Select("SELECT * FROM t_ui_template_version WHERE template_id = #{templateId} AND version_status = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1")
    TemplateVersionEntity findCurrentVersion(@Param("templateId") Long templateId);
}