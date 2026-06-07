package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryConfigUpdateRequest {
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
