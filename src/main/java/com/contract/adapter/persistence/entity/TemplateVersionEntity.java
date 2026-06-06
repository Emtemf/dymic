package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

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

    @TableField(fill = FieldFill.INSERT)
    private String createdName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
