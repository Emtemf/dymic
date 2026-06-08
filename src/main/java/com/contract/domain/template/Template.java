package com.contract.domain.template;

import com.contract.common.exception.BizException;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 模板领域模型
 * 充血模型，包含业务方法
 */
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private TemplateStatus status;
    private Long currentVersionId;
    private Long createdBy;
    private String createdName;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;

    public enum TemplateStatus {
        ENABLED, DISABLED
    }

    private Template() {}

    /**
     * 创建模板（工厂方法）
     */
    public static Template create(String templateCode, String templateName, String templateDesc, String bizType) {
        if (templateCode == null || templateCode.isBlank()) {
            throw new BizException("模板编码不能为空");
        }
        if (templateName == null || templateName.isBlank()) {
            throw new BizException("模板名称不能为空");
        }
        Template t = new Template();
        t.templateCode = templateCode;
        t.templateName = templateName;
        t.templateDesc = templateDesc;
        t.bizType = bizType;
        t.status = TemplateStatus.ENABLED;
        return t;
    }

    /**
     * 停用模板
     */
    public void disable() {
        if (status == TemplateStatus.DISABLED) {
            throw new BizException("模板已是停用状态");
        }
        this.status = TemplateStatus.DISABLED;
    }

    /**
     * 启用模板
     */
    public void enable() {
        if (status == TemplateStatus.ENABLED) {
            throw new BizException("模板已是启用状态");
        }
        this.status = TemplateStatus.ENABLED;
    }

    /**
     * 设置当前版本
     */
    public void setCurrentVersion(Long versionId) {
        this.currentVersionId = versionId;
    }

    /**
     * 判断模板是否启用
     */
    public boolean isEnabled() {
        return status == TemplateStatus.ENABLED;
    }

    /**
     * 判断模板是否停用
     */
    public boolean isDisabled() {
        return status == TemplateStatus.DISABLED;
    }

    /**
     * 判断是否可发布版本
     */
    public boolean canPublish() {
        return isEnabled();
    }

    // Getters
    public Long getId() { return id; }
    public String getTemplateCode() { return templateCode; }
    public String getTemplateName() { return templateName; }
    public String getTemplateDesc() { return templateDesc; }
    public String getBizType() { return bizType; }
    public TemplateStatus getStatus() { return status; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Reconstitute from persistence (used by Repository only)
     */
    public static Template reconstitute(Long id, String templateCode, String templateName, String templateDesc,
                                        String bizType, TemplateStatus status, Long currentVersionId,
                                        Long createdBy, String createdName, OffsetDateTime createdAt,
                                        Long updatedBy, String updatedName, OffsetDateTime updatedAt) {
        Template t = new Template();
        t.id = id;
        t.templateCode = templateCode;
        t.templateName = templateName;
        t.templateDesc = templateDesc;
        t.bizType = bizType;
        t.status = status;
        t.currentVersionId = currentVersionId;
        t.createdBy = createdBy;
        t.createdName = createdName;
        t.createdAt = createdAt;
        t.updatedBy = updatedBy;
        t.updatedName = updatedName;
        t.updatedAt = updatedAt;
        return t;
    }

    // Public setters (needed for MapStruct/Repository conversions)
    public void setId(Long id) { this.id = id; }
    public void setTemplateDesc(String templateDesc) { this.templateDesc = templateDesc; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public void setCurrentVersionId(Long currentVersionId) { this.currentVersionId = currentVersionId; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public void setCreatedName(String createdName) { this.createdName = createdName; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedName(String updatedName) { this.updatedName = updatedName; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
