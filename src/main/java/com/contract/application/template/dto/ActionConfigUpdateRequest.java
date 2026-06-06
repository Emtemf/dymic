package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 动作配置更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfigUpdateRequest {

    private String actionName;      // 动作名称

    private Long bindQueryId;       // 绑定查询ID

    private Integer confirmRequired; // 是否需要确认

    private String confirmText;     // 确认提示

    private String beforeRule;      // 前置规则（JSON）

    private String afterRule;       // 后置规则（JSON）

    private String propsJson;       // 扩展属性（JSON）

    private Integer sortNo;         // 排序号
}