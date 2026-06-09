package com.contract.adapter.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionConfigUpdateReq {
    private String actionName;
    private Long bindQueryId;
    private Integer confirmRequired;
    private String confirmText;
    private String beforeRule;
    private String afterRule;
    private String propsJson;
    private Integer sortNo;
}
