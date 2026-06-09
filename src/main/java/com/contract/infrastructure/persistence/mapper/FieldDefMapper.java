package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.FieldDefEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FieldDefMapper {
    int insertFieldDef(FieldDefEntity entity);

    FieldDefEntity selectByIdValue(@Param("id") Long id);

    List<FieldDefEntity> selectByVersionId(@Param("versionId") Long versionId);

    int updateFieldDef(FieldDefEntity entity);

    long countByFieldCode(@Param("fieldCode") String fieldCode);

    long countByFieldPath(@Param("fieldPath") String fieldPath);

    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}
