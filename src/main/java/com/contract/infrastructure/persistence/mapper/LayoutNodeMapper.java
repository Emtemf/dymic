package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.LayoutNodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LayoutNodeMapper {
    int insertLayoutNode(LayoutNodeEntity entity);

    LayoutNodeEntity selectByIdValue(@Param("id") Long id);

    List<LayoutNodeEntity> selectByVersionId(@Param("versionId") Long versionId);

    List<LayoutNodeEntity> selectByParentId(@Param("parentId") Long parentId);

    int updateLayoutNode(LayoutNodeEntity entity);

    LayoutNodeEntity selectByNodeCode(@Param("nodeCode") String nodeCode);

    int deleteByIdValue(@Param("id") Long id);

    int physicalDeleteByVersionId(@Param("versionId") Long versionId);
}
