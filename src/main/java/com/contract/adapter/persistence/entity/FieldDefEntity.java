package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_field_def", autoResultMap = true)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String validateRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
