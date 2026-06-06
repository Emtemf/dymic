package com.contract.adapter.controller;

import com.contract.application.template.SchemaService;
import com.contract.application.template.dto.SchemaDTO;
import com.contract.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Schema聚合API
 * 获取完整配置树用于前端渲染
 */
@Slf4j
@RestController
@RequestMapping("/api/templates/{templateId}/versions/{versionId}/schema")
@RequiredArgsConstructor
public class SchemaController {
    private final SchemaService schemaService;

    /**
     * 获取完整配置树（用于前端渲染）
     *
     * @param templateId 模板ID
     * @param versionId  版本ID
     * @return 完整配置树DTO
     */
    @GetMapping
    public Result<SchemaDTO> getSchema(
        @PathVariable Long templateId,
        @PathVariable Long versionId
    ) {
        log.info("API call: GET /api/templates/{}/versions/{}/schema", templateId, versionId);
        SchemaDTO schema = schemaService.getSchema(templateId, versionId);
        return Result.ok(schema);
    }
}