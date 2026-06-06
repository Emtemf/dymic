package com.contract.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 布局节点实体
 * Corresponds to table: t_ui_layout_node
 */
@Data
@TableName("t_ui_layout_node")
public class LayoutNodeEntity {

    @TableId(type = IdType.ASSIGN_ID)
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

    // Grid layout fields
    private Integer gridX;

    private Integer gridY;

    private Integer gridW;

    private Integer gridH;

    private Integer rowNo;

    private Integer colNo;

    private Integer colSpan;

    private Integer rowSpan;

    // Bind fields
    private String bindType;

    private Long bindRefId;

    // Rule fields (JSON stored as String)
    private String visibleRule;

    private String readonlyRule;

    private String propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}