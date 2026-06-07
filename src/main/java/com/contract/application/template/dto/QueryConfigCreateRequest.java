package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 查询配置创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigCreateRequest {
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
