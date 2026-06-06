package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_field_component", autoResultMap = true)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String requiredRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String readonlyRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String visibleRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
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
