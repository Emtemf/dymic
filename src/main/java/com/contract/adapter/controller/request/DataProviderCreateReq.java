package com.contract.adapter.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataProviderCreateReq {
    @NotBlank
    private String providerCode;

    @NotBlank
    private String providerName;

    @NotBlank
    private String providerType;

    @NotBlank
    private String configJson;
}
