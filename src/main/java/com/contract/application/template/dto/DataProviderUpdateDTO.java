package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 数据提供方更新 DTO
 */
@Data
public class DataProviderUpdateDTO {
    private String providerName;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
}