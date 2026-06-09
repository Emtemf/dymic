package com.contract.adapter.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldComponentCreateReq {
    @NotNull(message = "布局节点ID不能为空")
    private Long layoutNodeId;

    @NotNull(message = "字段定义ID不能为空")
    private Long fieldDefId;

    @NotBlank(message = "组件类型不能为空")
    private String componentType;

    private String labelName;
    private String placeholder;
    private String requiredRule;
    private String readonlyRule;
    private String visibleRule;
    private String componentProps;
    private String dataSourceType;
    private String staticOptionsJson;
    private String dictType;
    private Long dataProviderId;
    private Integer sortNo;
}
