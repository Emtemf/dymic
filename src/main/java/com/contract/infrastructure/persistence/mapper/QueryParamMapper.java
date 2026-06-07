package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryParamEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 查询参数Mapper
 */
@Mapper
public interface QueryParamMapper {
    @Insert("INSERT INTO t_ui_query_param (id, query_config_id, param_name, param_label, bind_source, " +
            "bind_path, component_type, required, default_value, sort_no, created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{queryConfigId}, #{paramName}, #{paramLabel}, #{bindSource}, " +
            "#{bindPath}, #{componentType}, #{required}, #{defaultValue}, #{sortNo}, #{createdAt}, #{updatedAt}, 0)")
    int insert(QueryParamEntity entity);

    @Select("SELECT * FROM t_ui_query_param WHERE query_config_id = #{queryConfigId} AND is_deleted = 0 ORDER BY sort_no")
    List<QueryParamEntity> findByQueryConfigId(@Param("queryConfigId") Long queryConfigId);

    @Update("UPDATE t_ui_query_param SET is_deleted = 1, updated_at = NOW() WHERE query_config_id = #{queryConfigId}")
    int deleteByQueryConfigId(@Param("queryConfigId") Long queryConfigId);
}
