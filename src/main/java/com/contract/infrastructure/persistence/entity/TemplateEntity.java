package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板实体
 * Corresponds to table: t_ui_template
 */
@Data
@TableName("t_ui_template")
public class TemplateEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String templateCode;

    private String templateName;

    private String templateDesc;

    private String bizType;

    private String status;

    private Long currentVersionId;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(exist = false)
    private String createdName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(exist = false)
    private String updatedName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}