package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据提供方实体
 * Corresponds to table: t_ui_data_provider
 */
@Data
public class DataProviderEntity {

    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String dataSourceCategory;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private Integer isTemporary;
    private String status;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
