package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

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
    private String status;
}