package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

/**
 * 查询回填规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRuleEntity {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
