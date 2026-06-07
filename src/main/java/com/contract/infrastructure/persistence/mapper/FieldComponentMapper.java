package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FieldComponentMapper extends BaseMapper<FieldComponentEntity> {

    @Delete("DELETE FROM t_ui_field_component WHERE template_version_id = #{versionId}")
    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}