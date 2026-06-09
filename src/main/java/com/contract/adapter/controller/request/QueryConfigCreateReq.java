package com.contract.adapter.controller.request;

import com.contract.application.template.dto.QueryFillRuleDTO;
import com.contract.application.template.dto.QueryParamDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryConfigCreateReq {
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
