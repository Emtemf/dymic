package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 字段定义实体
 * Corresponds to table: t_ui_field_def
 */
@Data
public class FieldDefEntity {

    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long detailTableId;
    private String fieldCode;
    private String fieldPath;
    private String fieldNameCn;
    private String fieldNameEn;
    private String dataType;
    private String valueType;
    private Integer requiredDefault;
    private Integer searchable;
    private Integer indexable;
    private String searchIndexColumn;
    private String defaultValue;
    private String validateRule;
    private String propsJson;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer isDeleted;
}
