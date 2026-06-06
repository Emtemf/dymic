package com.contract.adapter.controller;

import com.contract.application.template.FieldComponentService;
import com.contract.application.template.dto.FieldComponentDTO;
import com.contract.application.template.dto.FieldComponentUpdateDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/field-components")
@RequiredArgsConstructor
public class FieldComponentController {
    private final FieldComponentService service;

    @GetMapping("/{id}")
    public Result<FieldComponentDTO> getById(@PathVariable Long id) {
        FieldComponentDTO result = service.getById(id);
        return Result.ok(result);
    }

    @PutMapping("/{id}")
    public Result<FieldComponentDTO> update(
        @PathVariable Long id,
        @RequestBody FieldComponentUpdateDTO dto
    ) {
        FieldComponentDTO result = service.update(id, dto);
        return Result.ok(result);
    }
}