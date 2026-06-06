package com.contract.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 动作配置创建请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfigCreateRequest {

    @NotBlank(message = "动作名称不能为空")
    private String actionName;      // 保存、查询、提交等

    @NotBlank(message = "动作类型不能为空")
    private String actionType;      // SAVE/QUERY/CANCEL/CUSTOM

    @NotNull(message = "绑定节点ID不能为空")
    private Long bindNodeId;        // 按钮放在哪个节点

    private Long bindQueryId;       // 可选，查询按钮绑定查询ID

    private Integer confirmRequired; // 是否需要确认（0/1）

    private String confirmText;     // 确认提示

    private String beforeRule;      // 前置规则（JSON）

    private String afterRule;       // 后置规则（JSON）

    private String propsJson;       // 扩展属性（JSON）

    private Integer sortNo;         // 默认0
}