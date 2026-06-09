package com.contract.adapter.controller;

import com.contract.adapter.controller.request.QueryConfigCreateReq;
import com.contract.adapter.controller.request.QueryConfigUpdateReq;
import com.contract.application.template.QueryConfigService;
import com.contract.application.template.dto.*;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
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
            @Valid @RequestBody QueryConfigCreateReq req) {
        QueryConfigCreateRequest request = new QueryConfigCreateRequest();
        request.setQueryCode(req.getQueryCode());
        request.setQueryName(req.getQueryName());
        request.setQueryType(req.getQueryType());
        request.setDataProviderId(req.getDataProviderId());
        request.setTriggerType(req.getTriggerType());
        request.setResultMode(req.getResultMode());
        request.setBindNodeId(req.getBindNodeId());
        request.setPageSize(req.getPageSize());
        request.setPropsJson(req.getPropsJson());
        request.setParams(req.getParams());
        request.setFillRules(req.getFillRules());
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
            @Valid @RequestBody QueryConfigUpdateReq req) {
        QueryConfigUpdateRequest request = new QueryConfigUpdateRequest();
        request.setQueryName(req.getQueryName());
        request.setQueryType(req.getQueryType());
        request.setDataProviderId(req.getDataProviderId());
        request.setTriggerType(req.getTriggerType());
        request.setResultMode(req.getResultMode());
        request.setBindNodeId(req.getBindNodeId());
        request.setPageSize(req.getPageSize());
        request.setPropsJson(req.getPropsJson());
        request.setParams(req.getParams());
        request.setFillRules(req.getFillRules());
        return Result.ok(service.update(id, request));
    }

    @DeleteMapping("/query-configs/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok(null);
    }
}
