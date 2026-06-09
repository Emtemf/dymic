package com.contract.adapter.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionConfigCreateReq {
    @NotBlank(message = "动作名称不能为空")
    private String actionName;

    @NotBlank(message = "动作类型不能为空")
    private String actionType;

    @NotNull(message = "绑定节点ID不能为空")
    private Long bindNodeId;

    private Long bindQueryId;
    private Integer confirmRequired;
    private String confirmText;
    private String beforeRule;
    private String afterRule;
    private String propsJson;
    private Integer sortNo;
}
