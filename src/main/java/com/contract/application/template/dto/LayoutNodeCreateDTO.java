package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 布局节点创建 DTO
 */
@Data
public class LayoutNodeCreateDTO {
    private Long parentId;
    private String nodeType;
    private String displayName;
    private Integer sortNo;
    private Integer levelNo;

    // Grid 布局属性
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    // 绑定属性
    private String bindType;
    private Long bindRefId;

    // 规则配置
    private String visibleRule;
    private String readonlyRule;
    private String propsJson;
}