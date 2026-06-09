package com.contract.infrastructure.persistence.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 布局节点实体
 * Corresponds to table: t_ui_layout_node
 */
@Data
public class LayoutNodeEntity {

    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long parentId;
    private String nodeCode;
    private String nodeName;
    private String nodeType;
    private Integer sortNo;
    private Integer levelNo;
    private String nodePath;
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;
    private String bindType;
    private Long bindRefId;
    private String visibleRule;
    private String readonlyRule;
    private String propsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}
