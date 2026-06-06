package com.contract.domain.template;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 字段组件绑定领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldComponent {
    private Long id;
    private Long templateId;
    private Long templateVersionId;
    private Long layoutNodeId;        // 绑定的布局节点ID
    private Long fieldDefId;          // 绑定的字段定义ID

    private String componentType;     // INPUT/SELECT/DATE/MONEY/NUMBER/RADIO/CHECKBOX/TREE/UPLOAD
    private String labelName;         // 显示名称
    private String placeholder;       // 输入提示
    private Integer sortNo;           // 排序号

    private String requiredRule;      // JSON格式必填规则
    private String readonlyRule;      // JSON格式只读规则
    private String visibleRule;       // JSON格式显隐规则
    private String componentProps;    // JSON格式组件属性（选项、配置等）

    private Long dataProviderId;      // 数据提供方ID（用于SELECT等）

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 支持的组件类型
     */
    public static final Set<String> SUPPORTED_COMPONENT_TYPES = Set.of(
        "INPUT",      // 文本输入框
        "SELECT",     // 下拉选择框
        "DATE",       // 日期选择器
        "DATETIME",   // 日期时间选择器
        "MONEY",      // 金额输入框
        "NUMBER",     // 数字输入框
        "RADIO",      // 单选框
        "CHECKBOX",   // 复选框
        "TREE",       // 树形选择器
        "UPLOAD",     // 文件上传
        "TEXTAREA",   // 多行文本框
        "RICHTEXT",   // 富文本编辑器
        "SWITCH",     // 开关
        "SLIDER",     // 滑块
        "RATE",       // 评分
        "COLOR",      // 颜色选择器
        "CASCADE"     // 级联选择器
    );

    /**
     * 验证组件类型是否有效
     *
     * @param componentType 组件类型
     * @return 是否为支持的组件类型
     */
    public static boolean isValidComponentType(String componentType) {
        return componentType != null && SUPPORTED_COMPONENT_TYPES.contains(componentType.toUpperCase());
    }

    /**
     * 验证当前组件类型是否有效
     *
     * @return 是否为支持的组件类型
     */
    public boolean hasValidComponentType() {
        return isValidComponentType(this.componentType);
    }

    /**
     * 获取规范化组件类型（大写）
     *
     * @return 大写的组件类型
     */
    public String getNormalizedComponentType() {
        return this.componentType != null ? this.componentType.toUpperCase() : null;
    }
}