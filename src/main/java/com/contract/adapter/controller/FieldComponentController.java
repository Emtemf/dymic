package com.contract.adapter.controller;

import com.contract.adapter.controller.request.FieldComponentCreateReq;
import com.contract.application.template.FieldComponentService;
import com.contract.application.template.dto.FieldComponentCreateRequest;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段组件绑定控制器
 */
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/field-components")
@RequiredArgsConstructor
public class FieldComponentController {
    private final FieldComponentService service;

    /**
     * 创建字段组件绑定
     *
     * @param templateId 模板ID
     * @param versionId  版本ID
     * @param req        创建请求
     * @return 创建的字段组件绑定
     */
    @PostMapping
    public Result<FieldComponentDTO> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @Valid @RequestBody FieldComponentCreateReq req
    ) {
        FieldComponentCreateRequest request = new FieldComponentCreateRequest();
        request.setLayoutNodeId(req.getLayoutNodeId());
        request.setFieldDefId(req.getFieldDefId());
        request.setComponentType(req.getComponentType());
        request.setLabelName(req.getLabelName());
        request.setPlaceholder(req.getPlaceholder());
        request.setRequiredRule(req.getRequiredRule());
        request.setReadonlyRule(req.getReadonlyRule());
        request.setVisibleRule(req.getVisibleRule());
        request.setComponentProps(req.getComponentProps());
        request.setDataSourceType(req.getDataSourceType());
        request.setStaticOptionsJson(req.getStaticOptionsJson());
        request.setDictType(req.getDictType());
        request.setDataProviderId(req.getDataProviderId());
        request.setSortNo(req.getSortNo());
        FieldComponentDTO result = service.create(templateId, versionId, request);
        return Result.ok(result);
    }

    /**
     * 根据版本ID查询字段组件绑定列表
     *
     * @param versionId 版本ID
     * @return 字段组件绑定列表
     */
    @GetMapping
    public Result<List<FieldComponentDTO>> list(@PathVariable Long versionId) {
        List<FieldComponentDTO> results = service.listByVersionId(versionId);
        return Result.ok(results);
    }

    /**
     * 根据ID查询字段组件绑定
     *
     * @param id 字段组件绑定ID
     * @return 字段组件绑定
     */
    @GetMapping("/{id}")
    public Result<FieldComponentDTO> getById(@PathVariable Long id) {
        FieldComponentDTO result = service.getById(id);
        return Result.ok(result);
    }

    /**
     * 更新字段组件绑定
     *
     * @param id      字段组件绑定ID
     * @param request 更新请求
     * @return 更新后的字段组件绑定
     */
    @PutMapping("/{id}")
    public Result<FieldComponentDTO> update(
        @PathVariable Long id,
        @Valid @RequestBody FieldComponentUpdateDTO request
    ) {
        FieldComponentDTO result = service.update(id, request);
        return Result.ok(result);
    }

    /**
     * 删除字段组件绑定
     *
     * @param id 字段组件绑定ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok(null);
    }
}
