package com.contract.adapter.controller;

import com.contract.application.template.TemplateService;
import com.contract.common.result.Result;
import com.contract.domain.template.Template;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 模板管理 REST Controller
 * 提供模板的创建、查询、启用、停用等API
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    /**
     * 创建模板
     *
     * @param request 创建模板请求
     * @return 创建成功的模板信息
     */
    @PostMapping
    public Result<Template> create(@Valid @RequestBody CreateTemplateRequest request) {
        Template template = templateService.createTemplate(
            request.getTemplateCode(),
            request.getTemplateName(),
            request.getTemplateDesc(),
            request.getBizType()
        );
        return Result.ok(template);
    }

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板信息
     */
    @GetMapping("/{id}")
    public Result<Template> getById(@PathVariable Long id) {
        Template template = templateService.getById(id);
        return Result.ok(template);
    }

    /**
     * 根据编码查询模板
     *
     * @param code 模板编码
     * @return 模板信息
     */
    @GetMapping("/code/{code}")
    public Result<Template> getByCode(@PathVariable String code) {
        Template template = templateService.getByCode(code);
        return Result.ok(template);
    }

    /**
     * 停用模板（通过ID）
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @PostMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        templateService.disable(id);
        return Result.ok(null);
    }

    /**
     * 停用模板（通过编码）
     *
     * @param code 模板编码
     * @return 操作结果
     */
    @PostMapping("/code/{code}/disable")
    public Result<Void> disableByCode(@PathVariable String code) {
        Template template = templateService.getByCode(code);
        templateService.disable(template.getId());
        return Result.ok(null);
    }

    /**
     * 启用模板（通过ID）
     *
     * @param id 模板ID
     * @return 操作结果
     */
    @PostMapping("/{id}/enable")
    public Result<Void> enable(@PathVariable Long id) {
        templateService.enable(id);
        return Result.ok(null);
    }

    /**
     * 启用模板（通过编码）
     *
     * @param code 模板编码
     * @return 操作结果
     */
    @PostMapping("/code/{code}/enable")
    public Result<Void> enableByCode(@PathVariable String code) {
        Template template = templateService.getByCode(code);
        templateService.enable(template.getId());
        return Result.ok(null);
    }

    /**
     * 创建模板请求DTO
     */
    @Data
    public static class CreateTemplateRequest {
        private String templateCode;
        private String templateName;
        private String templateDesc;
        private String bizType;
    }
}
