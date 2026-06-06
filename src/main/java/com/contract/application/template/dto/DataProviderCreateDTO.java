package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 数据提供方创建 DTO
 */
@Data
public class DataProviderCreateDTO {
    private String providerCode;
    private String providerName;
    private String providerType;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private Integer isTemporary;       // 是否临时数据源（0/1）
}