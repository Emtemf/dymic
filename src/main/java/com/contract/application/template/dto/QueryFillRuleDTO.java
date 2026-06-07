package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 查询回填规则DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryFillRuleDTO {
    private Long id;
    private Long queryConfigId;
    private String sourceField;
    private String targetScope;
    private String targetPath;
    private String fillMode;
    private String transformJson;
    private Integer sortNo;
}
