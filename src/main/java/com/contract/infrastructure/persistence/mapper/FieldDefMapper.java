package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.FieldDefEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FieldDefMapper extends BaseMapper<FieldDefEntity> {

    @Delete("DELETE FROM t_ui_field_def WHERE template_version_id = #{versionId}")
    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}