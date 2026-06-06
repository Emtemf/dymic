package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板领域模型
 * 充血模型，包含业务方法
 */
@Data
public class Template {
    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;

    /**
     * 创建模板（工厂方法）
     */
    public static Template create(String templateCode, String templateName, String templateDesc, String bizType) {
        Template template = new Template();
        template.setTemplateCode(templateCode);
        template.setTemplateName(templateName);
        template.setTemplateDesc(templateDesc);
        template.setBizType(bizType);
        template.setStatus("ENABLED");
        return template;
    }

    /**
     * 停用模板
     */
    public void disable() {
        this.status = "DISABLED";
    }

    /**
     * 启用模板
     */
    public void enable() {
        this.status = "ENABLED";
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
        return "ENABLED".equals(this.status);
    }

    /**
     * 判断模板是否停用
     */
    public boolean isDisabled() {
        return "DISABLED".equals(this.status);
    }
}
