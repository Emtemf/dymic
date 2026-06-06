package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 字段组件绑定 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldComponentDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long fieldDefId;
    private Long layoutNodeId;
    private String componentType;
    private String labelName;
    private String placeholder;
    private Integer sortNo;

    // 扩展字段（规则配置）
    private String requiredRule;    // JSON格式动态必填规则
    private String readonlyRule;    // JSON格式动态只读规则
    private String visibleRule;     // JSON格式动态显隐规则
    private String componentProps;  // JSON格式组件自定义属性
    private Long dataProviderId;    // 数据源绑定

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}