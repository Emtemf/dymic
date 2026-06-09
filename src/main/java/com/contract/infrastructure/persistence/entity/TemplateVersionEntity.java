package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板版本实体
 * Corresponds to table: t_ui_template_version
 */
@Data
public class TemplateVersionEntity {

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
    private Integer isDeleted;
}
