package com.contract.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LayoutNodeMapper extends BaseMapper<LayoutNodeEntity> {

    @Delete("DELETE FROM t_ui_layout_node WHERE template_version_id = #{versionId}")
    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}