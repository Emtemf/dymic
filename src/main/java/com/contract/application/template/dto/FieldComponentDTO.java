package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 字段组件绑定 DTO
 */
@Data
public class FieldComponentDTO {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long fieldDefId;
    private Long layoutNodeId;
    private String componentType;
    private String labelName;
    private String placeholder;
    private Integer sortNo;
    private String componentProps;     // JSONB as String
    private Long dataProviderId;
    private String requiredRule;       // JSONB as String
    private String visibleRule;        // JSONB as String
    private String readonlyRule;       // JSONB as String
}