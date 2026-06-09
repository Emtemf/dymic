package com.contract.domain.template;

import com.contract.common.exception.BizException;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.types.VersionStatus;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 模板版本聚合根
 *
 * ===== 领域统一业务语言 =====
 *
 * 【状态流转】
 *   创建 → DRAFT → PUBLISHED → DISABLED
 *                ↘ ARCHIVED
 *
 * 【状态转换规则】
 * - DRAFT → PUBLISHED：发布版本（管理员操作）
 * - PUBLISHED → DISABLED：停用版本（管理员操作）
 * - DRAFT/PUBLISHED → ARCHIVED：归档版本（管理员操作）
 *
 * 【业务规则】
 * 1. 只有DRAFT状态才能发布
 * 2. 发布后记录发布时间和发布人
 *
 * 【聚合边界】
 * - TemplateVersion是聚合根
 * - 包含值对象：VersionStatus、AuditInfo
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
    private AuditInfo auditInfo;

    private TemplateVersion() {}

    /**
     * 创建草稿版本（工厂方法）
     *
     * 【前置条件】
     * - templateId != null
     * - versionNo > 0
     *
     * 【后置条件】
     * - versionStatus == DRAFT
     */
    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        if (templateId == null) {
            throw new BizException("模板ID不能为空");
        }
        if (versionNo == null || versionNo <= 0) {
            throw new BizException("版本号必须大于0");
        }

        TemplateVersion v = new TemplateVersion();
        v.templateId = templateId;
        v.versionNo = versionNo;
        v.versionName = versionName;
        v.versionStatus = VersionStatus.DRAFT;
        v.auditInfo = AuditInfo.create();
        return v;
    }

    /**
     * 发布版本
     *
     * 【前置条件】
     * - versionStatus == DRAFT
     *
     * 【后置条件】
     * - versionStatus == PUBLISHED
     * - publishTime != null
     *
     * @throws BizException 如果状态不是DRAFT
     */
    public void publish(Long publishBy) {
        if (versionStatus != VersionStatus.DRAFT) {
            throw new BizException("只有草稿状态才能发布，当前状态：" + versionStatus.getDisplayName());
        }
        this.versionStatus = VersionStatus.PUBLISHED;
        this.publishTime = OffsetDateTime.now();
        this.publishBy = publishBy;
        this.auditInfo = auditInfo.update();
    }

    /**
     * 停用版本
     *
     * 【前置条件】
     * - versionStatus == PUBLISHED
     *
     * 【后置条件】
     * - versionStatus == DISABLED
     */
    public void disable() {
        if (versionStatus != VersionStatus.PUBLISHED) {
            throw new BizException("只有已发布状态才能停用");
        }
        this.versionStatus = VersionStatus.DISABLED;
        this.auditInfo = auditInfo.update();
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
    public AuditInfo getAuditInfo() { return auditInfo; }

    // 便捷方法（向后兼容）
    public Long getCreatedBy() { return auditInfo != null ? auditInfo.getCreatedBy() : null; }
    public String getCreatedName() { return auditInfo != null ? auditInfo.getCreatedName() : null; }
    public OffsetDateTime getCreatedAt() { return auditInfo != null ? auditInfo.getCreatedAt() : null; }
    public Long getUpdatedBy() { return auditInfo != null ? auditInfo.getUpdatedBy() : null; }
    public String getUpdatedName() { return auditInfo != null ? auditInfo.getUpdatedName() : null; }
    public OffsetDateTime getUpdatedAt() { return auditInfo != null ? auditInfo.getUpdatedAt() : null; }

    /**
     * Reconstitute from persistence
     */
    public static TemplateVersion reconstitute(
        Long id, Long templateId, Integer versionNo, String versionName,
        VersionStatus versionStatus, OffsetDateTime publishTime, Long publishBy,
        String schemaHash, String remark, AuditInfo auditInfo
    ) {
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
        v.auditInfo = auditInfo;
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TemplateVersion that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
