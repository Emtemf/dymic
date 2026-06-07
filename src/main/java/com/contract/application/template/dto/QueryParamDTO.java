package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 查询参数DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParamDTO {
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
}
