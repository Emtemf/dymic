package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.ActionConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActionConfigMapper {
    int insertActionConfig(ActionConfigEntity entity);

    ActionConfigEntity selectByIdValue(@Param("id") Long id);

    List<ActionConfigEntity> selectByTemplateVersionId(@Param("templateVersionId") Long templateVersionId);

    int updateActionConfig(ActionConfigEntity entity);

    int deleteByIdValue(@Param("id") Long id);

    int physicalDeleteByVersionId(@Param("versionId") Long versionId);

    List<ActionConfigEntity> selectAllActionConfigs();
}
