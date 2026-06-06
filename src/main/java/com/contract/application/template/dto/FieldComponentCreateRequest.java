package com.contract.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 字段组件绑定创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldComponentCreateRequest {

    @NotNull(message = "布局节点ID不能为空")
    private Long layoutNodeId;

    @NotNull(message = "字段定义ID不能为空")
    private Long fieldDefId;

    @NotBlank(message = "组件类型不能为空")
    private String componentType;   // INPUT/SELECT/DATE/MONEY/NUMBER/RADIO/CHECKBOX等

    private String labelName;       // 可选，默认使用fieldNameCn

    private String placeholder;     // 可选

    // 扩展字段（可选）
    private String requiredRule;    // JSON格式动态必填规则

    private String readonlyRule;    // JSON格式动态只读规则

    private String visibleRule;     // JSON格式动态显隐规则

    private String componentProps;  // JSON格式组件自定义属性

    private Long dataProviderId;    // 数据源绑定（下拉框等）

    private Integer sortNo;         // 可选，默认0
}