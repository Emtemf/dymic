package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段组件绑定领域模型
 */
@Data
public class FieldComponent {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long fieldDefId;
    private Long layoutNodeId;
    private String componentType;      // INPUT/SELECT/DATE/MONEY/NUMBER
    private String labelName;          // 显示名称
    private String placeholder;        // 输入提示
    private Integer sortNo;            // 排序号
    private String componentProps;     // JSONB: 组件属性(选项、配置等)
    private Long dataProviderId;       // 数据提供方 ID
    private String requiredRule;       // JSONB: 必填规则
    private String visibleRule;        // JSONB: 显隐规则
    private String readonlyRule;       // JSONB: 只读规则
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}