package com.contract.domain.template;

import com.contract.common.exception.BizException;
import com.contract.domain.shared.types.AuditInfo;
import com.contract.domain.template.types.*;
import java.util.Objects;

/**
 * 模板聚合根
 *
 * ===== 领域统一业务语言 =====
 *
 * 【状态流转】
 *   创建 → ENABLED ⇄ DISABLED
 *
 * 【状态转换规则】
 * - ENABLED → DISABLED：停用模板（管理员操作）
 * - DISABLED → ENABLED：启用模板（管理员操作）
 *
 * 【业务规则】
 * 1. 模板编码唯一
 * 2. 模板名称不能为空
 * 3. 只有ENABLED状态才能发布版本
 *
 * 【聚合边界】
 * - Template是聚合根
 * - 包含实体：无（TemplateVersion是独立聚合根）
 * - 包含值对象：TemplateId、TemplateCode、TemplateName、TemplateStatus、AuditInfo
 */
public class Template {
    private TemplateId id;
    private TemplateCode templateCode;
    private TemplateName templateName;
    private TemplateDesc templateDesc;
    private BizType bizType;
    private TemplateStatus status;
    private Long currentVersionId;
    private AuditInfo auditInfo;

    private Template() {}  // 私有构造器

    /**
     * 创建模板（工厂方法）
     *
     * 【前置条件】
     * - templateCode不为空
     * - templateName不为空
     *
     * 【后置条件】
     * - status == ENABLED
     *
     * @return Template实例
     * @throws BizException 如果参数无效
     */
    public static Template create(
        TemplateCode templateCode,
        TemplateName templateName,
        TemplateDesc templateDesc,
        BizType bizType
    ) {
        Template template = new Template();
        template.templateCode = templateCode;
        template.templateName = templateName;
        template.templateDesc = templateDesc;
        template.bizType = bizType;
        template.status = TemplateStatus.ENABLED;
        template.auditInfo = AuditInfo.create();
        return template;
    }

    /**
     * 停用模板
     *
     * 【前置条件】
     * - status == ENABLED
     *
     * 【后置条件】
     * - status == DISABLED
     *
     * @throws BizException 如果状态不是ENABLED
     */
    public void disable() {
        if (status != TemplateStatus.ENABLED) {
            throw new BizException("只有启用状态才能停用，当前状态：" + status.getDisplayName());
        }
        this.status = TemplateStatus.DISABLED;
        this.auditInfo = auditInfo.update();
    }

    /**
     * 启用模板
     *
     * 【前置条件】
     * - status == DISABLED
     *
     * 【后置条件】
     * - status == ENABLED
     *
     * @throws BizException 如果状态不是DISABLED
     */
    public void enable() {
        if (status != TemplateStatus.DISABLED) {
            throw new BizException("只有停用状态才能启用，当前状态：" + status.getDisplayName());
        }
        this.status = TemplateStatus.ENABLED;
        this.auditInfo = auditInfo.update();
    }

    /**
     * 设置当前版本
     *
     * 【前置条件】
     * - versionId != null
     *
     * 【后置条件】
     * - currentVersionId == versionId
     */
    public void setCurrentVersion(Long versionId) {
        this.currentVersionId = versionId;
        this.auditInfo = auditInfo.update();
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

    // Getters（不暴露setter）
    public TemplateId getId() { return id; }
    public TemplateCode getTemplateCode() { return templateCode; }
    public TemplateName getTemplateName() { return templateName; }
    public TemplateDesc getTemplateDesc() { return templateDesc; }
    public BizType getBizType() { return bizType; }
    public TemplateStatus getStatus() { return status; }
    public Long getCurrentVersionId() { return currentVersionId; }
    public AuditInfo getAuditInfo() { return auditInfo; }

    // ===== 便捷方法（向后兼容） =====
    /**
     * 获取ID值（原始类型）
     */
    public Long getIdValue() {
        return id != null ? id.getValue() : null;
    }

    /**
     * 获取模板编码值（原始类型）
     */
    public String getTemplateCodeValue() {
        return templateCode != null ? templateCode.getValue() : null;
    }

    /**
     * 获取模板名称值（原始类型）
     */
    public String getTemplateNameValue() {
        return templateName != null ? templateName.getValue() : null;
    }

    /**
     * 获取模板描述值（原始类型）
     */
    public String getTemplateDescValue() {
        return templateDesc != null ? templateDesc.getValue() : null;
    }

    /**
     * 获取业务类型值（原始类型）
     */
    public String getBizTypeValue() {
        return bizType != null ? bizType.getValue() : null;
    }

    /**
     * 获取状态值（原始类型）
     */
    public String getStatusValue() {
        return status != null ? status.name() : null;
    }

    /**
     * 获取创建人ID（审计信息）
     */
    public Long getCreatedBy() {
        return auditInfo != null ? auditInfo.getCreatedBy() : null;
    }

    /**
     * 获取创建人名称（审计信息）
     */
    public String getCreatedName() {
        return auditInfo != null ? auditInfo.getCreatedName() : null;
    }

    /**
     * 获取创建时间（审计信息）
     */
    public java.time.OffsetDateTime getCreatedAt() {
        return auditInfo != null ? auditInfo.getCreatedAt() : null;
    }

    /**
     * 获取更新人ID（审计信息）
     */
    public Long getUpdatedBy() {
        return auditInfo != null ? auditInfo.getUpdatedBy() : null;
    }

    /**
     * 获取更新人名称（审计信息）
     */
    public String getUpdatedName() {
        return auditInfo != null ? auditInfo.getUpdatedName() : null;
    }

    /**
     * 获取更新时间（审计信息）
     */
    public java.time.OffsetDateTime getUpdatedAt() {
        return auditInfo != null ? auditInfo.getUpdatedAt() : null;
    }

    /**
     * Reconstitute from persistence (used by MapStruct only)
     */
    public static Template reconstitute(
        TemplateId id,
        TemplateCode templateCode,
        TemplateName templateName,
        TemplateDesc templateDesc,
        BizType bizType,
        TemplateStatus status,
        Long currentVersionId,
        AuditInfo auditInfo
    ) {
        Template template = new Template();
        template.id = id;
        template.templateCode = templateCode;
        template.templateName = templateName;
        template.templateDesc = templateDesc;
        template.bizType = bizType;
        template.status = status;
        template.currentVersionId = currentVersionId;
        template.auditInfo = auditInfo;
        return template;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Template that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
