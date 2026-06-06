package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 数据提供方领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProvider {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String configJson;
    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
    private String status;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;

    public static DataProvider create(String providerCode, String providerName, String providerType) {
        DataProvider provider = new DataProvider();
        provider.setProviderCode(providerCode);
        provider.setProviderName(providerName);
        provider.setProviderType(providerType);
        provider.setStatus("ENABLED");
        return provider;
    }
}