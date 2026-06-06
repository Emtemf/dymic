package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_data_provider", autoResultMap = true)
public class DataProviderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String providerCode;
    private String providerName;
    private String providerType;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object configJson;

    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private String status;

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
