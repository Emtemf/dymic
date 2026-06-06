package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据提供方实体
 * Corresponds to table: t_ui_data_provider
 */
@Data
@TableName("t_ui_data_provider")
public class DataProviderEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String providerCode;

    private String providerName;

    private String providerType;

    private String configJson;

    private Integer cacheEnabled;

    private Integer cacheTtlSeconds;

    private Integer isTemporary;       // 是否临时数据源（0/1）

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