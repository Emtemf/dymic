package com.contract.adapter.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "t_ui_layout_node", autoResultMap = true)
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

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object visibleRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object readonlyRule;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Object propsJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
