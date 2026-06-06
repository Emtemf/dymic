package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段组件绑定实体
 * Corresponds to table: t_ui_field_component
 */
@Data
@TableName("t_ui_field_component")
public class FieldComponentEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;

    private Long templateVersionId;

    private Long layoutNodeId;

    private Long fieldDefId;

    private String componentType;

    private String labelName;

    private String placeholder;

    // Rule fields (JSON stored as String)
    private String requiredRule;

    private String readonlyRule;

    private String visibleRule;

    private String componentProps;

    private Long dataProviderId;

    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}