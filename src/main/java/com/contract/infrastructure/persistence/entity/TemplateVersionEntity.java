package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板版本实体
 * Corresponds to table: t_ui_template_version
 */
@Data
@TableName("t_ui_template_version")
public class TemplateVersionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;

    private Integer versionNo;

    private String versionName;

    private String versionStatus;

    private LocalDateTime publishTime;

    private Long publishBy;

    private String schemaHash;

    private String remark;

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