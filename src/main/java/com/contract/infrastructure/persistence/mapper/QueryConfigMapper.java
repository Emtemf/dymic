package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryConfigEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 查询配置Mapper
 */
@Mapper
public interface QueryConfigMapper {
    @Insert("INSERT INTO t_ui_query_config (id, template_id, template_version_id, query_code, query_name, " +
            "query_type, data_provider_id, trigger_type, result_mode, bind_node_id, page_size, props_json, " +
            "created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{templateId}, #{templateVersionId}, #{queryCode}, #{queryName}, " +
            "#{queryType}, #{dataProviderId}, #{triggerType}, #{resultMode}, #{bindNodeId}, #{pageSize}, #{propsJson}, " +
            "#{createdAt}, #{updatedAt}, 0)")
    int insert(QueryConfigEntity entity);

    @Select("SELECT * FROM t_ui_query_config WHERE id = #{id} AND is_deleted = 0")
    QueryConfigEntity findById(@Param("id") Long id);

    @Select("SELECT * FROM t_ui_query_config WHERE template_version_id = #{versionId} AND is_deleted = 0 ORDER BY created_at")
    List<QueryConfigEntity> findByTemplateVersionId(@Param("versionId") Long versionId);

    @Update("UPDATE t_ui_query_config SET query_name = #{queryName}, query_type = #{queryType}, " +
            "data_provider_id = #{dataProviderId}, trigger_type = #{triggerType}, result_mode = #{resultMode}, " +
            "bind_node_id = #{bindNodeId}, page_size = #{pageSize}, props_json = #{propsJson}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    int update(QueryConfigEntity entity);

    @Update("UPDATE t_ui_query_config SET is_deleted = 1, updated_at = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
