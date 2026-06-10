package com.contract.adapter.controller;

import com.contract.application.template.DataSourceQueryFacadeService;
import com.contract.application.template.dto.DataSourceQueryDTO;
import com.contract.application.template.dto.OptionDataDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/ui/data-sources")
@RequiredArgsConstructor
public class DataSourceQueryController {
    private final DataSourceQueryFacadeService service;

    @GetMapping("/query")
    public Result<List<DataSourceQueryDTO>> queryAll(@RequestParam(required = false) String type) {
        return Result.ok(type == null || type.isBlank() ? service.queryAll() : service.queryByType(type));
    }

    @GetMapping("/{providerId}/execute")
    public Result<List<OptionDataDTO>> execute(@PathVariable Long providerId) {
        return Result.ok(service.executeQuery(providerId));
    }
}
