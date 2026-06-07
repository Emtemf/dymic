package com.contract.adapter.controller;

import com.contract.application.template.QueryConfigService;
import com.contract.application.template.dto.*;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryConfigController {
    private final QueryConfigService service;

    @PostMapping("/templates/{templateId}/versions/{versionId}/query-configs")
    public Result<QueryConfigDTO> create(
            @PathVariable Long templateId,
            @PathVariable Long versionId,
            @RequestBody QueryConfigCreateRequest request) {
        return Result.ok(service.create(templateId, versionId, request));
    }

    @GetMapping("/query-configs/{id}")
    public Result<QueryConfigDTO> getById(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @GetMapping("/templates/{templateId}/versions/{versionId}/query-configs")
    public Result<List<QueryConfigDTO>> listByVersionId(
            @PathVariable Long templateId,
            @PathVariable Long versionId) {
        return Result.ok(service.listByVersionId(versionId));
    }

    @PutMapping("/query-configs/{id}")
    public Result<QueryConfigDTO> update(
            @PathVariable Long id,
            @RequestBody QueryConfigUpdateRequest request) {
        return Result.ok(service.update(id, request));
    }

    @DeleteMapping("/query-configs/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok(null);
    }
}
