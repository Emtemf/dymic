package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 模板实体
 * Corresponds to table: t_ui_template
 */
@Data
public class TemplateEntity {

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateDesc;
    private String bizType;
    private String status;
    private Long currentVersionId;
    private Long createdBy;
    private String createdName;
    private OffsetDateTime createdAt;
    private Long updatedBy;
    private String updatedName;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
