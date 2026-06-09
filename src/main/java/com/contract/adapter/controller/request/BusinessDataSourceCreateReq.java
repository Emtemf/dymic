package com.contract.adapter.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessDataSourceCreateReq {
    @NotBlank(message = "数据源名称不能为空")
    private String providerName;

    @NotBlank(message = "数据源类型不能为空")
    private String providerType;

    @NotBlank(message = "配置JSON不能为空")
    private String configJson;

    private Integer cacheEnabled;
    private Integer cacheTtlSeconds;
}
