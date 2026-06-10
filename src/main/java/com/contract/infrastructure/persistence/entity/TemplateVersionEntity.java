package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

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
    private Integer isDeleted;
}
