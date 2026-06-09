package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据提供方 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProviderDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private Integer isTemporary;       // 是否临时数据源（0/1）
    private String status;

    // 审计字段
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;
}