package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
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
    private String nodeCode;       // 自动生成：jiBenXinXi
    private String nodeName;       // 节点名称（中文）
    private String nodeType;       // CARD_CONTAINER/SEPARATOR
    private Integer sortNo;        // 显示顺序
    private Integer levelNo;       // 层级
    private String nodePath;       // 自动生成：root.jiBenXinXi

    // Grid布局字段（扩展）
    private Integer gridX;
    private Integer gridY;
    private Integer gridW;
    private Integer gridH;
    private Integer rowNo;
    private Integer colNo;
    private Integer colSpan;
    private Integer rowSpan;

    // 绑定字段（扩展）
    private String bindType;       // DATASOURCE/QUERY/FIELD
    private Long bindRefId;        // 绑定对象ID

    // 规则字段（扩展）
    private String visibleRule;    // JSON格式显隐规则
    private String readonlyRule;   // JSON格式只读规则
    private String propsJson;      // 扩展属性

    // 审计字段
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 自动生成节点编码
     * 规则：nodeName（中文）→ nodeCode（拼音驼峰）
     * 示例："基本信息" → "jiBenXinXi"
     */
    public void generateNodeCode() {
        if (this.nodeName != null && !this.nodeName.trim().isEmpty()) {
            this.nodeCode = PinyinUtils.generateNodeCode(this.nodeName);
        }
    }

    /**
     * 自动生成节点路径
     * 规则：parentNodePath + "." + nodeCode
     * 示例："root.jiBenXinXi" + "." + "heTongMingCheng" → "root.jiBenXinXi.heTongMingCheng"
     *
     * @param parentNodePath 父节点路径
     */
    public void generateNodePath(String parentNodePath) {
        if (this.nodeCode != null) {
            this.nodePath = PinyinUtils.generateNodePath(parentNodePath, this.nodeCode);
        }
    }

    /**
     * 创建布局节点（工厂方法）
     */
    public static LayoutNode create(String nodeType, String displayName) {
        LayoutNode node = new LayoutNode();
        node.setNodeType(nodeType);
        node.setNodeName(displayName);
        node.generateNodeCode();
        return node;
    }
}