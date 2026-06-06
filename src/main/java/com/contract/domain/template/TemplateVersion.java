package com.contract.domain.template;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板版本领域模型
 * 充血模型，包含业务方法
 */
@Data
public class TemplateVersion {
    private Long id;
    private Long templateId;
    private Integer versionNo;
    private String versionName;
    private String versionStatus;
    private LocalDateTime publishTime;
    private Long publishBy;
    private String schemaHash;
    private String remark;
    private Long createdBy;
    private String createdName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private LocalDateTime updatedAt;

    /**
     * 创建草稿版本（工厂方法）
     */
    public static TemplateVersion createDraft(Long templateId, Integer versionNo, String versionName) {
        TemplateVersion version = new TemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNo(versionNo);
        version.setVersionName(versionName);
        version.setVersionStatus("DRAFT");
        return version;
    }

    /**
     * 发布版本
     * 只有草稿状态才能发布
     */
    public void publish(Long publishBy) {
        if (!isDraft()) {
            throw new IllegalStateException("只有草稿状态才能发布");
        }
        this.versionStatus = "PUBLISHED";
        this.publishTime = LocalDateTime.now();
        this.publishBy = publishBy;
    }

    /**
     * 停用版本
     */
    public void disable() {
        this.versionStatus = "DISABLED";
    }

    /**
     * 判断是否为草稿状态
     */
    public boolean isDraft() {
        return "DRAFT".equals(this.versionStatus);
    }

    /**
     * 判断是否为已发布状态
     */
    public boolean isPublished() {
        return "PUBLISHED".equals(this.versionStatus);
    }

    /**
     * 判断是否为停用状态
     */
    public boolean isDisabled() {
        return "DISABLED".equals(this.versionStatus);
    }

    /**
     * 判断是否可以发布
     */
    public boolean canPublish() {
        return isDraft();
    }
}
