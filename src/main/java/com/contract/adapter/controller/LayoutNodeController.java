package com.contract.adapter.controller;

import com.contract.application.template.LayoutNodeService;
import com.contract.application.template.dto.LayoutNodeDTO;
import com.contract.application.template.dto.LayoutNodeCreateDTO;
import com.contract.application.template.dto.LayoutNodeUpdateDTO;
import com.contract.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 布局节点配置 API
 */
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/layout-nodes")
@RequiredArgsConstructor
public class LayoutNodeController {
    private final LayoutNodeService service;

    /**
     * 创建布局节点
     */
    @PostMapping
    public Result<LayoutNodeDTO> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @Valid @RequestBody LayoutNodeCreateDTO dto
    ) {
        LayoutNodeDTO result = service.create(templateId, versionId, dto);
        return Result.ok(result);
    }

    /**
     * 根据ID查询布局节点
     */
    @GetMapping("/{id}")
    public Result<LayoutNodeDTO> getById(@PathVariable Long id) {
        LayoutNodeDTO result = service.getById(id);
        return Result.ok(result);
    }

    /**
     * 根据模板版本ID查询布局节点列表
     */
    @GetMapping
    public Result<List<LayoutNodeDTO>> list(@PathVariable Long versionId) {
        List<LayoutNodeDTO> result = service.listByVersionId(versionId);
        return Result.ok(result);
    }

    /**
     * 更新布局节点
     */
    @PutMapping("/{id}")
    public Result<LayoutNodeDTO> update(
        @PathVariable Long id,
        @Valid @RequestBody LayoutNodeUpdateDTO dto
    ) {
        LayoutNodeDTO result = service.update(id, dto);
        return Result.ok(result);
    }

    /**
     * 删除布局节点
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return Result.ok();
    }
}