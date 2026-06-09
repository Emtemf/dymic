package com.contract.domain.template.types;

/**
 * 模板状态枚举
 */
public enum TemplateStatus {
    ENABLED("启用"),
    DISABLED("停用");

    private final String displayName;

    TemplateStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
