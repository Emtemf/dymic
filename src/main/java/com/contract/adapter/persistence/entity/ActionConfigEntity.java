package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 动作配置实体
 */
@Data
@TableName(value = "t_ui_action_config", autoResultMap = true)
public class ActionConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private Long templateVersionId;
    private String actionCode;
    private String actionName;
    private String actionType;
    private Long bindNodeId;
    private Long bindQueryId;
    private Integer confirmRequired;
    private String confirmText;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object beforeRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object afterRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object propsJson;

    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}