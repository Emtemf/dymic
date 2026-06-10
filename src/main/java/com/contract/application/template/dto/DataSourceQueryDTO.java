package com.contract.application.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceQueryDTO {
    private Long id;
    private String providerCode;
    private String providerName;
    private String providerType;
    private String dataSourceCategory;
    private String configSource;
    private String configJson;
    private Boolean isTree;
}
