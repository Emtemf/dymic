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
    private Map<String, Object> componentProps;
    private Long dataProviderId;
    private Map<String, Object> requiredRule;
    private Map<String, Object> visibleRule;
    private Map<String, Object> readonlyRule;
}