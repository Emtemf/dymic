package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 字段组件绑定实体
 * Corresponds to table: t_ui_field_component
 */
@Data
public class FieldComponentEntity {

    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;
    private Long fieldDefId;
    private String componentType;
    private String labelName;
    private String placeholder;
    private String requiredRule;
    private String readonlyRule;
    private String visibleRule;
    private String componentProps;
    private Long dataProviderId;
    private Integer sortNo;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
