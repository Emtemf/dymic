package com.contract.infrastructure.persistence.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 查询参数实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParamEntity {
    private Long id;
    private Long queryConfigId;
    private String paramName;
    private String paramLabel;
    private String bindSource;
    private String bindPath;
    private String componentType;
    private Integer required;
    private String defaultValue;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
