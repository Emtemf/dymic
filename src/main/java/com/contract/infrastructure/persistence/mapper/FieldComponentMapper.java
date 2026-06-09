package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.FieldComponentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FieldComponentMapper {
    int insertFieldComponent(FieldComponentEntity entity);

    FieldComponentEntity selectByIdValue(@Param("id") Long id);

    List<FieldComponentEntity> selectByVersionId(@Param("versionId") Long versionId);

    List<FieldComponentEntity> selectByFieldDefId(@Param("fieldDefId") Long fieldDefId);

    List<FieldComponentEntity> selectByLayoutNodeId(@Param("layoutNodeId") Long layoutNodeId);

    int updateFieldComponent(FieldComponentEntity entity);

    int deleteByIdValue(@Param("id") Long id);

    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}
