package com.contract.application.template.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDataSourceRequest {
    @NotBlank(message = "数据源名称不能为空")
    private String providerName;

    @NotBlank(message = "数据源类型不能为空")
    private String providerType;

    @NotBlank(message = "配置JSON不能为空")
    private String configJson;

    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
}
