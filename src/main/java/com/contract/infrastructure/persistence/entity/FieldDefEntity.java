package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字段定义实体
 * Corresponds to table: t_ui_field_def
 */
@Data
@TableName("t_ui_field_def")
public class FieldDefEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;

    private Long templateVersionId;

    private Long detailTableId;

    private String fieldCode;

    private String fieldPath;

    private String fieldNameCn;

    private String fieldNameEn;

    private String dataType;

    private String valueType;

    private Integer requiredDefault;

    private Integer searchable;

    private Integer indexable;

    private String searchIndexColumn;

    private String defaultValue;

    // JSON fields stored as String
    private String validateRule;

    private String propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}