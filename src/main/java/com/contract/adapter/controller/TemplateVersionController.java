package com.contract.adapter.controller;

import com.contract.application.template.TemplateService;
import com.contract.application.template.TemplateVersionService;
import com.contract.common.result.Result;
import com.contract.domain.template.Template;
import com.contract.domain.template.TemplateVersion;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 模板版本管理 REST Controller
 * 提供版本的创建、发布、查询等API
 */
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateVersionController {

    private final TemplateVersionService versionService;
    private final TemplateService templateService;

    /**
     * 创建草稿版本
     *
     * @param templateId 模板ID
     * @param request 创建版本请求
     * @return 创建成功的版本信息
     */
    @PostMapping("/{templateId}/versions")
    public Result<TemplateVersion> createDraft(
        @PathVariable Long templateId,
        @Valid @RequestBody CreateVersionRequest request
    ) {
        TemplateVersion version = versionService.createDraft(
            templateId,
            request.getVersionNo(),
            request.getVersionName()
        );
        return Result.ok(version);
    }

    /**
     * 发布版本
     *
     * @param versionId 版本ID
     * @return 操作结果
     */
    @PostMapping("/versions/{versionId}/publish")
    public Result<Void> publish(@PathVariable Long versionId) {
        // TODO: 从用户上下文获取实际的用户ID
        Long publishBy = 1001L;
        versionService.publish(versionId, publishBy);
        return Result.ok(null);
    }

    /**
     * 获取模板的当前发布版本
     *
     * @param templateId 模板ID
     * @return 当前版本信息，如果没有发布版本则返回null
     */
    @GetMapping("/{templateId}/versions/current")
    public Result<TemplateVersion> getCurrentVersion(@PathVariable Long templateId) {
        TemplateVersion version = versionService.findCurrentVersion(templateId);
        return Result.ok(version);
    }

    /**
     * 根据版本ID查询版本信息
     *
     * @param versionId 版本ID
     * @return 版本信息
     */
    @GetMapping("/versions/{versionId}")
    public Result<TemplateVersion> getVersionById(@PathVariable Long versionId) {
        TemplateVersion version = versionService.getById(versionId);
        return Result.ok(version);
    }

    /**
     * 查询模板的所有版本列表
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    @GetMapping("/{templateId}/versions")
    public Result<List<TemplateVersion>> listVersions(@PathVariable Long templateId) {
        List<TemplateVersion> versions = versionService.findByTemplateId(templateId);
        return Result.ok(versions);
    }

    /**
     * 通过模板编码创建草稿版本
     * 这是一个便捷方法，允许通过模板编码而不是ID来创建版本
     *
     * @param templateCode 模板编码
     * @param request 创建版本请求
     * @return 创建成功的版本信息
     */
    @PostMapping("/code/{templateCode}/versions")
    public Result<TemplateVersion> createDraftByCode(
        @PathVariable String templateCode,
        @Valid @RequestBody CreateVersionRequest request
    ) {
        Template template = templateService.getByCode(templateCode);
        TemplateVersion version = versionService.createDraft(
            template.getId(),
            request.getVersionNo(),
            request.getVersionName()
        );
        return Result.ok(version);
    }

    /**
     * 通过模板编码获取当前发布版本
     *
     * @param templateCode 模板编码
     * @return 当前版本信息
     */
    @GetMapping("/code/{templateCode}/versions/current")
    public Result<TemplateVersion> getCurrentVersionByCode(@PathVariable String templateCode) {
        Template template = templateService.getByCode(templateCode);
        TemplateVersion version = versionService.findCurrentVersion(template.getId());
        return Result.ok(version);
    }

    /**
     * 创建版本请求DTO
     */
    @Data
    public static class CreateVersionRequest {
        private Integer versionNo;
        private String versionName;
    }
}
