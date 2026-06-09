package com.contract.adapter.controller;

import com.contract.adapter.controller.request.BusinessDataSourceCreateReq;
import com.contract.application.template.DataSourceConfigService;
import com.contract.application.template.dto.BusinessDataSourceRequest;
import com.contract.application.template.dto.DataProviderDTO;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/config/business-data-sources")
@RequiredArgsConstructor
public class BusinessDataSourceController {
    private final DataSourceConfigService service;

    @PostMapping
    public Result<DataProviderDTO> create(@Valid @RequestBody BusinessDataSourceCreateReq req) {
        BusinessDataSourceRequest request = BusinessDataSourceRequest.builder()
            .providerName(req.getProviderName())
            .providerType(req.getProviderType())
            .configJson(req.getConfigJson())
            .cacheEnabled(req.getCacheEnabled())
            .cacheTtlSeconds(req.getCacheTtlSeconds())
            .build();
        return Result.ok(service.create(request));
    }

    @GetMapping
    public Result<List<DataProviderDTO>> list() {
        return Result.ok(service.listBusinessConfigs());
    }
}
