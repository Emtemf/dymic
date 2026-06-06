package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 动作配置 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfigDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;

    private String actionCode;        // 自动生成：baoCun
    private String actionName;        // 动作名称（中文）：保存
    private String actionType;        // SAVE/QUERY/CANCEL/CUSTOM

    private Long bindNodeId;          // 绑定的布局节点ID
    private Long bindQueryId;         // 绑定的查询配置ID

    private Integer confirmRequired;  // 是否需要确认（0/1）
    private String confirmText;       // 确认提示文本

    private String beforeRule;        // JSON格式执行前规则
    private String afterRule;         // JSON格式执行后规则
    private String propsJson;         // JSON格式扩展属性

    private Integer sortNo;           // 显示顺序

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}