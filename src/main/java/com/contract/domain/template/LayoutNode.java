package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 布局节点领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutNode {
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

    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 创建布局节点（工厂方法）
     */
    public static LayoutNode create(String nodeType, String displayName) {
        LayoutNode node = new LayoutNode();
        node.setNodeType(nodeType);
        node.setNodeName(displayName);
        return node;
    }
}