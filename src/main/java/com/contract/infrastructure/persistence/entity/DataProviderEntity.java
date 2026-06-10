package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

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
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
