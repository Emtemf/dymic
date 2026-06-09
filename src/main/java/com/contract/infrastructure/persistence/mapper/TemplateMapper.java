package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.TemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板Mapper
 */
@Mapper
public interface TemplateMapper {
    int insertTemplate(TemplateEntity entity);

    TemplateEntity selectByIdValue(@Param("id") Long id);

    TemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    List<TemplateEntity> selectAllTemplates();

    int updateTemplate(TemplateEntity entity);

    long countByTemplateCode(@Param("templateCode") String templateCode);
}
