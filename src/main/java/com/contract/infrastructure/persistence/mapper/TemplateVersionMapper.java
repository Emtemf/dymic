package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.TemplateVersionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板版本Mapper
 */
@Mapper
public interface TemplateVersionMapper {
    int insertTemplateVersion(TemplateVersionEntity entity);

    TemplateVersionEntity selectByIdValue(@Param("id") Long id);

    TemplateVersionEntity selectByTemplateIdAndVersionNo(@Param("templateId") Long templateId, @Param("versionNo") Integer versionNo);

    TemplateVersionEntity findCurrentVersion(@Param("templateId") Long templateId);

    List<TemplateVersionEntity> selectByTemplateId(@Param("templateId") Long templateId);

    int updateTemplateVersion(TemplateVersionEntity entity);

    long countByTemplateIdAndVersionNo(@Param("templateId") Long templateId, @Param("versionNo") Integer versionNo);
}
