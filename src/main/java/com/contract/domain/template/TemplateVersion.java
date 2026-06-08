package com.contract.domain.template;

import com.contract.common.exception.BizException;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 模板版本领域模型
 * 充血模型，包含业务方法
 */
public class TemplateVersion {
    private Long id;
    private Long templateId;
    private Integer versionNo;
    private String versionName;
    private VersionStatus versionStatus;
    private OffsetDateTime publishTime;
    private Long publishBy;
    private String schemaHash;
    private String remark;
    private Long createdBy;
    private String createdName;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;

    public enum VersionStatus {
        DRAFT, PUBLISHED, DISABLED, ARCHIVED
    }

    private TemplateVersion() {}

    /**
     * 创建草稿版本（工厂方法）
     */
    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        TemplateVersion v = new TemplateVersion();
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = VersionStatus.DRAFT;
        return v;
    }

    /**
     * 发布版本
     * 只有草稿状态才能发布
     */
    public void publish(Long publishBy) {
        if (versionStatus != VersionStatus.DRAFT) {
            throw new BizException("只有草稿状态才能发布，当前状态：" + versionStatus);
        }
        this.versionStatus = VersionStatus.PUBLISHED;
        this.publishTime = OffsetDateTime.now();
        this.publishBy = publishBy;
    }

    /**
     * 停用版本
     */
    public void disable() {
        this.versionStatus = VersionStatus.DISABLED;
    }

    /**
     * 判断是否为草稿状态
     */
    public boolean isDraft() {
        return versionStatus == VersionStatus.DRAFT;
    }

    /**
     * 判断是否为已发布状态
     */
    public boolean isPublished() {
        return versionStatus == VersionStatus.PUBLISHED;
    }

    /**
     * 判断是否为停用状态
     */
    public boolean isDisabled() {
        return versionStatus == VersionStatus.DISABLED;
    }

    /**
     * 判断是否可以发布
     */
    public boolean canPublish() {
        return isDraft();
    }

    // Getters
    public Long getId() { return id; }
    public Long getTemplateId() { return templateId; }
    public Integer getVersionNo() { return versionNo; }
    public String getVersionName() { return versionName; }
    public VersionStatus getVersionStatus() { return versionStatus; }
    public OffsetDateTime getPublishTime() { return publishTime; }
    public Long getPublishBy() { return publishBy; }
    public String getSchemaHash() { return schemaHash; }
    public String getRemark() { return remark; }
    public Long getCreatedBy() { return createdBy; }
    public String getCreatedName() { return createdName; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Long getUpdatedBy() { return updatedBy; }
    public String getUpdatedName() { return updatedName; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    /**
     * Reconstitute from persistence (used by Repository only)
     */
    public static TemplateVersion reconstitute(Long id, Long templateId, Integer versionNo, String versionName,
                                               VersionStatus versionStatus, OffsetDateTime publishTime, Long publishBy,
                                               String schemaHash, String remark,
                                               Long createdBy, String createdName, OffsetDateTime createdAt,
                                               Long updatedBy, String updatedName, OffsetDateTime updatedAt) {
        TemplateVersion v = new TemplateVersion();
        v.id = id;
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = versionStatus;
        v.publishTime = publishTime;
        v.publishBy = publishBy;
        v.schemaHash = schemaHash;
        v.remark = remark;
        v.createdBy = createdBy;
        v.createdName = createdName;
        v.createdAt = createdAt;
        v.updatedBy = updatedBy;
        v.updatedName = updatedName;
        v.updatedAt = updatedAt;
        return v;
    }

    // Public setters (for MapStruct/Repository)
    public void setId(Long id) { this.id = id; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public void setVersionName(String versionName) { this.versionName = versionName; }
    public void setSchemaHash(String schemaHash) { this.schemaHash = schemaHash; }
    public void setRemark(String remark) { this.remark = remark; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public void setCreatedName(String createdName) { this.createdName = createdName; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public void setUpdatedName(String updatedName) { this.updatedName = updatedName; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateVersion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
