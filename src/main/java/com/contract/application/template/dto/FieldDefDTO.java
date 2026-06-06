package com.contract.application.template.dto;

import lombok.Data;

/**
 * 字段定义 DTO
 */
@Data
public class FieldDefDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private String fieldCode;
    private String fieldPath;
    private String fieldNameCn;
    private String dataType;
    private Integer requiredDefault;
}