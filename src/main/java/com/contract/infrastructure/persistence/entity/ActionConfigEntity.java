package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 动作配置实体
 * Corresponds to table: t_ui_action_config
 */
@Data
@TableName("t_ui_action_config")
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

    // Rule fields (JSON stored as String)
    private String beforeRule;

    private String afterRule;

    private String propsJson;

    private Integer sortNo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}