package com.contract.application.template.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 布局节点树DTO（嵌套结构）
 * 用于构建前端渲染的树形配置结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutNodeTreeDTO {
    private Long id;
    private String nodeCode;
    private String nodePath;
    private String nodeName;
    private String nodeType;       // CARD_CONTAINER/SEPARATOR
    private Integer sortNo;

    // 组件列表（该节点下的所有字段组件）
    private List<FieldComponentDTO> components;

    // 动作列表（该节点下的所有按钮）
    private List<ActionConfigDTO> actions;

    // 子节点列表（递归嵌套）
    private List<LayoutNodeTreeDTO> children;

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
    private String visibleRule;
    private String readonlyRule;
    private String propsJson;
}