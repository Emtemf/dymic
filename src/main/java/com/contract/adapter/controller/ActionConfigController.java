package com.contract.adapter.controller;

import com.contract.adapter.controller.request.ActionConfigCreateReq;
import com.contract.adapter.controller.request.ActionConfigUpdateReq;
import com.contract.application.template.ActionConfigService;
import com.contract.application.template.dto.ActionConfigCreateRequest;
import com.contract.application.template.dto.ActionConfigDTO;
import com.contract.application.template.dto.ActionConfigUpdateRequest;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动作配置 API
 */
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/action-configs")
@RequiredArgsConstructor
public class ActionConfigController {

    private final ActionConfigService service;

    /**
     * 创建动作配置
     */
    @PostMapping
    public Result<ActionConfigDTO> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @Valid @RequestBody ActionConfigCreateReq req
    ) {
        ActionConfigCreateRequest request = new ActionConfigCreateRequest();
        request.setActionName(req.getActionName());
        request.setActionType(req.getActionType());
        request.setBindNodeId(req.getBindNodeId());
        request.setBindQueryId(req.getBindQueryId());
        request.setConfirmRequired(req.getConfirmRequired());
        request.setConfirmText(req.getConfirmText());
        request.setBeforeRule(req.getBeforeRule());
        request.setAfterRule(req.getAfterRule());
        request.setPropsJson(req.getPropsJson());
        request.setSortNo(req.getSortNo());
        ActionConfigDTO result = service.create(templateId, versionId, request);
        return Result.ok(result);
    }

    /**
     * 根据ID查询动作配置
     */
    @GetMapping("/{configId}")
    public Result<ActionConfigDTO> getById(@PathVariable Long configId) {
        ActionConfigDTO result = service.getById(configId);
        return Result.ok(result);
    }

    /**
     * 根据模板版本ID查询动作配置列表
     */
    @GetMapping
    public Result<List<ActionConfigDTO>> list(@PathVariable Long versionId) {
        List<ActionConfigDTO> result = service.listByVersionId(versionId);
        return Result.ok(result);
    }

    /**
     * 更新动作配置
     */
    @PutMapping("/{configId}")
    public Result<ActionConfigDTO> update(
        @PathVariable Long configId,
        @Valid @RequestBody ActionConfigUpdateReq req
    ) {
        ActionConfigUpdateRequest request = new ActionConfigUpdateRequest();
        request.setActionName(req.getActionName());
        request.setBindQueryId(req.getBindQueryId());
        request.setConfirmRequired(req.getConfirmRequired());
        request.setConfirmText(req.getConfirmText());
        request.setBeforeRule(req.getBeforeRule());
        request.setAfterRule(req.getAfterRule());
        request.setPropsJson(req.getPropsJson());
        request.setSortNo(req.getSortNo());
        ActionConfigDTO result = service.update(configId, request);
        return Result.ok(result);
    }

    /**
     * 删除动作配置
     */
    @DeleteMapping("/{configId}")
    public Result<Void> delete(@PathVariable Long configId) {
        service.delete(configId);
        return Result.ok();
    }
}
