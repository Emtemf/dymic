package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 模板版本 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVersionDTO {
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
}
