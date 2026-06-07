package com.contract.domain.template;

import com.contract.common.util.PinyinUtils;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 动作配置领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionConfig {
    private Long id;
    private Long templateId;
    private Long templateVersionId;

    private String actionCode;        // 自动生成：baoCun
    private String actionName;        // 动作名称（中文）：保存
    private String actionType;        // SAVE/QUERY/CANCEL/CUSTOM

    private Long bindNodeId;          // 绑定的布局节点ID
    private Long bindQueryId;         // 绑定的查询配置ID

    private Integer confirmRequired;  // 是否需要确认（0/1）
    private String confirmText;       // 确认提示文本

    private String beforeRule;        // JSON格式执行前规则
    private String afterRule;         // JSON格式执行后规则
    private String propsJson;         // JSON格式扩展属性

    private Integer sortNo;           // 显示顺序

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;

    /**
     * 支持的动作类型
     */
    public static final String ACTION_TYPE_SAVE = "SAVE";
    public static final String ACTION_TYPE_QUERY = "QUERY";
    public static final String ACTION_TYPE_CANCEL = "CANCEL";
    public static final String ACTION_TYPE_CUSTOM = "CUSTOM";

    /**
     * 自动生成动作编码
     * 规则：actionName（中文）→ actionCode（拼音驼峰）
     * 示例："保存" → "baoCun"
     */
    public void generateActionCode() {
        if (this.actionName != null && !this.actionName.trim().isEmpty()) {
            this.actionCode = PinyinUtils.generateActionCode(this.actionName);
        }
    }

    /**
     * 创建动作配置（工厂方法）
     *
     * @param actionName 动作名称（中文）
     * @param actionType 动作类型
     * @return ActionConfig实例
     */
    public static ActionConfig create(String actionName, String actionType) {
        ActionConfig action = new ActionConfig();
        action.setActionName(actionName);
        action.setActionType(actionType);
        action.setConfirmRequired(0);
        action.setSortNo(0);
        action.generateActionCode();
        return action;
    }

    /**
     * 验证动作类型是否有效
     *
     * @param actionType 动作类型
     * @return 是否为支持的动作类型
     */
    public static boolean isValidActionType(String actionType) {
        if (actionType == null) {
            return false;
        }
        return ACTION_TYPE_SAVE.equals(actionType)
            || ACTION_TYPE_QUERY.equals(actionType)
            || ACTION_TYPE_CANCEL.equals(actionType)
            || ACTION_TYPE_CUSTOM.equals(actionType)
            || "SAVE_BUTTON".equals(actionType)
            || "QUERY_BUTTON".equals(actionType);
    }

    /**
     * 验证当前动作类型是否有效
     *
     * @return 是否为支持的动作类型
     */
    public boolean hasValidActionType() {
        return isValidActionType(this.actionType);
    }
}