package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 查询配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private String queryCode;
    private String queryName;
    private String queryType;
    private Long dataProviderId;
    private String triggerType;
    private String resultMode;
    private Long bindNodeId;
    private Integer pageSize;
    private String propsJson;
    private List<QueryParamDTO> params;
    private List<QueryFillRuleDTO> fillRules;
}
