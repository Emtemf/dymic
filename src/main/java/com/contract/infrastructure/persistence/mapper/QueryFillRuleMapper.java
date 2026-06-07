package com.contract.infrastructure.persistence.mapper;

import com.contract.infrastructure.persistence.entity.QueryFillRuleEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 查询回填规则Mapper
 */
@Mapper
public interface QueryFillRuleMapper {
    @Insert("INSERT INTO t_ui_query_fill_rule (id, query_config_id, source_field, target_scope, target_path, " +
            "fill_mode, transform_json, sort_no, created_at, updated_at, is_deleted) " +
            "VALUES (#{id}, #{queryConfigId}, #{sourceField}, #{targetScope}, #{targetPath}, " +
            "#{fillMode}, #{transformJson}, #{sortNo}, #{createdAt}, #{updatedAt}, 0)")
    int insert(QueryFillRuleEntity entity);

    @Select("SELECT * FROM t_ui_query_fill_rule WHERE query_config_id = #{queryConfigId} AND is_deleted = 0 ORDER BY sort_no")
    List<QueryFillRuleEntity> findByQueryConfigId(@Param("queryConfigId") Long queryConfigId);

    @Update("UPDATE t_ui_query_fill_rule SET is_deleted = 1, updated_at = NOW() WHERE query_config_id = #{queryConfigId}")
    int deleteByQueryConfigId(@Param("queryConfigId") Long queryConfigId);
}
