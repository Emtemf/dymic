package com.contract.adapter.controller;

import com.contract.application.template.FieldDefService;
import com.contract.application.template.dto.FieldDefCreateDTO;
import com.contract.application.template.dto.FieldDefCreateResult;
import com.contract.application.template.dto.FieldDefDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 字段定义 Controller
 */
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/field-defs")
@RequiredArgsConstructor
public class FieldDefController {
    private final FieldDefService service;

    @PostMapping
    public Result<FieldDefCreateResult> create(
        @PathVariable Long templateId,
        @PathVariable Long versionId,
        @RequestBody FieldDefCreateDTO dto
    ) {
        FieldDefCreateResult result = service.create(templateId, versionId, dto);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<FieldDefDTO> getById(@PathVariable Long id) {
        FieldDefDTO result = service.getById(id);
        return Result.ok(result);
    }

    @GetMapping
    public Result<List<FieldDefDTO>> list(@PathVariable Long versionId) {
        List<FieldDefDTO> result = service.listByVersionId(versionId);
        return Result.ok(result);
    }
}