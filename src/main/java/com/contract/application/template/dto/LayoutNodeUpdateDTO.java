package com.contract.application.template.dto;

import lombok.Data;
import java.util.Map;

/**
 * 布局节点更新 DTO
 */
@Data
public class LayoutNodeUpdateDTO {
    private String nodeName;
    private Integer sortNo;

    // Grid 布局属性
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    // 规则配置
    private Map<String, Object> visibleRule;
    private Map<String, Object> readonlyRule;
    private Map<String, Object> propsJson;
}